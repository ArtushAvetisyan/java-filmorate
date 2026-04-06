package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.dao.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.dao.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.dao.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;
import ru.yandex.practicum.filmorate.model.user.User;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({FilmDbStorage.class, UserDbStorage.class, UserMapper.class, FilmMapper.class, GenreMapper.class})
public class FilmDbStorageTest {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @Test
    void shouldCorrectAddFilm() {
        Film film = Film.builder()
                .name("Интерстеллар")
                .description("Фильм о путешествии в космос")
                .releaseDate(LocalDate.of(2014, 7, 16))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .genres(new LinkedHashSet<>(List.of(Genre.builder().id(1).name("Комедия").build())))
                .build();

        Film savedFilm = filmDbStorage.addFilm(film);
        Assertions.assertEquals("Интерстеллар", filmDbStorage.getFilmById(savedFilm.getId()).get().getName());
    }

    @Test
    void shouldCorrectReturnFilmById() {
        Film film = Film.builder()
                .name("Интерстеллар")
                .description("Фильм о путешествии в космос")
                .releaseDate(LocalDate.of(2014, 7, 16))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .genres(new LinkedHashSet<>(List.of(Genre.builder().id(1).name("Комедия").build())))
                .build();

        filmDbStorage.addFilm(film);
        Film filmFromDb = filmDbStorage.getFilmById(film.getId()).get();

        Assertions.assertEquals("Интерстеллар", filmFromDb.getName());
        Assertions.assertEquals("Фильм о путешествии в космос", filmFromDb.getDescription());
        Assertions.assertEquals("G", filmFromDb.getMpa().getName());
        Assertions.assertEquals(1, filmFromDb.getGenres().size());
        Assertions.assertEquals("Комедия", filmFromDb.getGenres().getFirst().getName());
    }

    @Test
    void shouldCorrectReturnAllFilms() {
        Film firstFilm = Film.builder()
                .name("Интерстеллар")
                .description("Фильм о путешествии в космос")
                .releaseDate(LocalDate.of(2014, 7, 16))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .build();

        Film secondFilm = Film.builder()
                .name("Время")
                .description("Американский фантастический триллер Эндрю Никкола")
                .releaseDate(LocalDate.of(2011, 10, 28))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .build();

        filmDbStorage.addFilm(firstFilm);
        filmDbStorage.addFilm(secondFilm);
        List<Film> films = filmDbStorage.getAllFilms();

        Assertions.assertEquals(2, films.size());
        Assertions.assertEquals("Интерстеллар", films.get(0).getName());
        Assertions.assertEquals("Время", films.get(1).getName());
    }

    @Test
    void shouldCorrectUpdateFilm() {
        Film film = Film.builder()
                .name("Интерстеллар")
                .description("Фильм о путешествии в космос")
                .releaseDate(LocalDate.of(2014, 7, 16))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .build();

        Film savedFilm = filmDbStorage.addFilm(film);
        filmDbStorage.update(Film.builder()
                .id(savedFilm.getId())
                .name("Интерстеллар 2")
                .description("Продолжение фильма о путешествии в космос")
                .releaseDate(LocalDate.of(2025, 7, 16))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .build());

        Film filmFromDb = filmDbStorage.getFilmById(savedFilm.getId()).get();

        Assertions.assertEquals("Интерстеллар 2", filmFromDb.getName());
        Assertions.assertEquals("Продолжение фильма о путешествии в космос", filmFromDb.getDescription());
        Assertions.assertEquals(LocalDate.of(2025, 7, 16), filmFromDb.getReleaseDate());
    }

    @Test
    void shouldCorrectAddLike() {
        Film film = Film.builder()
                .name("Интерстеллар")
                .description("Фильм о путешествии в космос")
                .releaseDate(LocalDate.of(2014, 7, 16))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .build();

        User user = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User savedUser = userDbStorage.createNewUser(user);
        Film savedFilm = filmDbStorage.addFilm(film);

        filmDbStorage.addLike(savedFilm.getId(), savedUser.getId());

        Assertions.assertEquals(1, filmDbStorage.getLikesList(savedFilm.getId()).size());
        Assertions.assertEquals(1, filmDbStorage.getLikesList(savedFilm.getId()).getFirst());
    }

    @Test
    void shouldCorrectReturnLikeList() {
        Film film = Film.builder()
                .name("Интерстеллар")
                .description("Фильм о путешествии в космос")
                .releaseDate(LocalDate.of(2014, 7, 16))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .build();

        User user = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User savedUser = userDbStorage.createNewUser(user);
        Film savedFilm = filmDbStorage.addFilm(film);
        filmDbStorage.addLike(savedFilm.getId(), savedUser.getId());

        Assertions.assertEquals(1, filmDbStorage.getLikesList(savedFilm.getId()).size());
    }

    @Test
    void shouldCorrectReturnMostPopularFilms() {
        Film firstFilm = Film.builder()
                .name("Интерстеллар")
                .description("Фильм о путешествии в космос")
                .releaseDate(LocalDate.of(2014, 7, 16))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .build();

        Film secondFilm = Film.builder()
                .name("Время")
                .description("Американский фантастический триллер Эндрю Никкола")
                .releaseDate(LocalDate.of(2011, 10, 28))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .build();

        Film thirdFilm = Film.builder()
                .name("Форсаж")
                .description("Коп под прикрытием внедряется в банду стритрейсеров и становится одним из них...")
                .releaseDate(LocalDate.of(2001, 9, 14))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .build();

        User firstUser = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User secondUser = User.builder()
                .name("Марк")
                .email("petrov@ya.ru")
                .login("qwerty2")
                .birthday(LocalDate.of(1993, 7, 10))
                .build();

        User thirdUser = User.builder()
                .name("Анастасия")
                .email("petrova@ya.ru")
                .login("qwerty3")
                .birthday(LocalDate.of(1994, 7, 10))
                .build();

        userDbStorage.createNewUser(firstUser);
        userDbStorage.createNewUser(secondUser);
        userDbStorage.createNewUser(thirdUser);
        filmDbStorage.addFilm(firstFilm);
        filmDbStorage.addFilm(secondFilm);
        filmDbStorage.addFilm(thirdFilm);
        filmDbStorage.addLike(firstFilm.getId(), firstUser.getId());
        filmDbStorage.addLike(firstFilm.getId(), secondUser.getId());
        filmDbStorage.addLike(firstFilm.getId(), thirdUser.getId());
        filmDbStorage.addLike(secondFilm.getId(), firstUser.getId());
        filmDbStorage.addLike(secondFilm.getId(), secondUser.getId());
        filmDbStorage.addLike(thirdFilm.getId(), thirdUser.getId());


        List<Film> mostPopularFilms = filmDbStorage.getMostPopularFilms(3);

        Assertions.assertEquals("Интерстеллар", mostPopularFilms.getFirst().getName());
        Assertions.assertEquals("Время", mostPopularFilms.get(1).getName());
        Assertions.assertEquals("Форсаж", mostPopularFilms.get(2).getName());
    }

    @Test
    void shouldCorrectDeleteLike() {
        Film film = Film.builder()
                .name("Интерстеллар")
                .description("Фильм о путешествии в космос")
                .releaseDate(LocalDate.of(2014, 7, 16))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .build();

        User user = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User savedUser = userDbStorage.createNewUser(user);
        Film savedFilm = filmDbStorage.addFilm(film);

        filmDbStorage.addLike(savedFilm.getId(), savedUser.getId());
        Assertions.assertEquals(1, filmDbStorage.getLikesList(savedFilm.getId()).size());

        filmDbStorage.deleteLike(savedFilm.getId(), savedUser.getId());
        Assertions.assertEquals(0, filmDbStorage.getLikesList(savedFilm.getId()).size());
    }

    @Test
    void shouldReturnEmptyOptionalWhenFilmNotFound() {
        Assertions.assertTrue(filmDbStorage.getFilmById(1).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenUpdateNonExistentFilm() {
        Film film = Film.builder()
                .name("Интерстеллар")
                .description("Фильм о путешествии в космос")
                .releaseDate(LocalDate.of(2014, 7, 16))
                .duration(169)
                .mpa(new Mpa(1, "G"))
                .genres(new LinkedHashSet<>(List.of(Genre.builder().id(1).name("Комедия").build())))
                .build();

        Assertions.assertThrows(NotFoundException.class, () -> filmDbStorage.update(film));
    }
}
