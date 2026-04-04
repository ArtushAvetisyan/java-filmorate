package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class FilmServiceTest {
    private InMemoryUserStorage userStorage;
    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        filmService = new FilmService(new InMemoryFilmStorage(), userService);
    }

    @Test
    void shouldGetFilmByIdSuccessfully() {
        filmService.createFilm(Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(199))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build());

        filmService.createFilm(Film.builder()
                .name("Время")
                .description("a".repeat(199))
                .releaseDate(LocalDate.of(2020, 11, 7))
                .duration(190)
                .build());

        Film film = filmService.getFilmById(2);

        Assertions.assertEquals("Время", film.getName());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFilmDoesNotExist() {
        int incorrectId = 1;
        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                filmService.getFilmById(incorrectId));

        Assertions.assertEquals("Фильм с id - " + incorrectId + " не найден", exception.getMessage());
    }

    @Test
    void shouldAddLikeSuccessfully() {
        User user = userService.create(User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build());

        Film film = filmService.createFilm(Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(199))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build());

        filmService.addLike(user.getId(), film.getId());
        Set<Long> likes = film.getLikes();

        Assertions.assertEquals(1, film.getLikes().size());
        Assertions.assertTrue(likes.contains(user.getId()));
    }

    @Test
    void addLikeShouldFailWithInvalidUserId() {
        int incorrectId = 1;
        Film film = filmService.createFilm(Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(199))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build());

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                filmService.addLike(incorrectId, film.getId()));

        Assertions.assertEquals("Пользователь с id - " + incorrectId + " не найден", exception.getMessage());
    }

    @Test
    void addLikeShouldFailWithInvalidFilmId() {
        int incorrectFilmId = 1;
        User user = userService.create(User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build());

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                filmService.addLike(user.getId(), incorrectFilmId));

        Assertions.assertEquals("Фильм с id - " + incorrectFilmId + " не найден", exception.getMessage());
    }

    @Test
    void shouldDeleteLikeSuccessfully() {
        User user = userService.create(User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build());

        Film film = filmService.createFilm(Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(199))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build());

        filmService.addLike(user.getId(), film.getId());
        Assertions.assertEquals(1, film.getLikes().size());

        filmService.deleteLike(user.getId(), film.getId());
        Assertions.assertEquals(0, film.getLikes().size());
    }

    @Test
    void deleteLikeShouldFailWithInvalidUserId() {
        int incorrectId = 1;
        Film film = filmService.createFilm(Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(199))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build());

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                filmService.deleteLike(incorrectId, film.getId()));

        Assertions.assertEquals("Пользователь с id - " + incorrectId + " не найден", exception.getMessage());
    }

    @Test
    void deleteLikeShouldFailWithInvalidFilmId() {
        int incorrectFilmId = 1;
        User user = userService.create(User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build());

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                filmService.deleteLike(user.getId(), incorrectFilmId));

        Assertions.assertEquals("Фильм с id - " + incorrectFilmId + " не найден", exception.getMessage());
    }

    @Test
    void shouldReturnMostPopularLikes() {
        Film firstFilm = filmService.createFilm(Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(199))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build());

        Film secondFilm = filmService.createFilm(Film.builder()
                .name("Время")
                .description("a".repeat(199))
                .releaseDate(LocalDate.of(2020, 11, 7))
                .duration(190)
                .build());

        Film thirdFilm = filmService.createFilm(Film.builder()
                .name("Побег из Шоушенка")
                .description("a".repeat(199))
                .releaseDate(LocalDate.of(1994, 1, 9))
                .duration(200)
                .build());

        User firstUser = userService.create(User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build());

        User secondUser = userService.create(User.builder()
                .name("Petr")
                .login("qwerty1")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build());

        User thirdUser = userService.create(User.builder()
                .name("Vasily")
                .login("qwerty3")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build());

        filmService.addLike(firstUser.getId(), firstFilm.getId());
        filmService.addLike(secondUser.getId(), firstFilm.getId());
        filmService.addLike(thirdUser.getId(), firstFilm.getId());

        filmService.addLike(firstUser.getId(), secondFilm.getId());
        filmService.addLike(secondUser.getId(), secondFilm.getId());

        filmService.addLike(firstUser.getId(), thirdFilm.getId());

        List<Film> mostPopularLikes = filmService.getMostPopularLikes(10);

        Assertions.assertEquals("Интерстеллар", mostPopularLikes.getFirst().getName());
        Assertions.assertEquals("Время", mostPopularLikes.get(1).getName());
        Assertions.assertEquals("Побег из Шоушенка", mostPopularLikes.getLast().getName());
    }
}
