package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    @Test
    void shouldReturnAllFilms() {
        List<Film> movies = List.of(
                Film.builder()
                        .name("Интерстеллар")
                        .description("a".repeat(199))
                        .releaseDate(LocalDate.of(2014, 11, 7))
                        .duration(169)
                        .build(),

                Film.builder()
                        .name("Время")
                        .description("a".repeat(199))
                        .releaseDate(LocalDate.of(2020, 11, 7))
                        .duration(170)
                        .build(),

                Film.builder()
                        .name("Звёздные войны")
                        .description("a".repeat(199))
                        .releaseDate(LocalDate.of(2008, 1, 7))
                        .duration(200)
                        .build());

        filmController.addFilm(movies.getFirst());
        filmController.addFilm(movies.get(1));
        filmController.addFilm(movies.getLast());
        List<Film> movieList = new ArrayList<>(filmController.getAllFilms());

        Assertions.assertEquals(3, movieList.size());
        Assertions.assertEquals("Интерстеллар", movieList.getFirst().getName());
        Assertions.assertEquals("Время", movieList.get(1).getName());
        Assertions.assertEquals("Звёздные войны", movieList.getLast().getName());
    }

    @Test
    public void shouldCreateFilmIfDataIsCorrect() {
        Film newFilm = Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(199))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build();
        Film film = filmController.addFilm(newFilm);

        Assertions.assertEquals(1, filmController.getAllFilms().size());
        Assertions.assertEquals("Интерстеллар", film.getName());
    }

    @Test
    public void shouldReturnExceptionIfNameIsIncorrect() {
        Film newFilm = Film.builder()
                .name("")
                .description("a".repeat(199))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build();
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.addFilm(newFilm));

        Assertions.assertEquals("Название фильмы не может быть пустым", exception.getMessage());
    }

    @Test
    public void shouldReturnExceptionIfDescriptionIsMoreThan200Symbols() {
        Film filmWith201Symbol = Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(201))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build();
        Film filmWith200Symbols = Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(200))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build();
        filmController.addFilm(filmWith200Symbols);
        ValidationException exception1 = assertThrows(ValidationException.class, () -> filmController.addFilm(filmWith201Symbol));

        Assertions.assertEquals("Описание не может превышать 200 символов", exception1.getMessage());
        Assertions.assertEquals(1, filmController.getAllFilms().size());
    }

    @Test
    public void shouldReturnExceptionIfReleaseDateIsIncorrect() {
        Film filmWithCorrectDate = Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(200))
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(169)
                .build();
        Film filmWithIncorrectDate = Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(200))
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(169)
                .build();
        filmController.addFilm(filmWithCorrectDate);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.addFilm(filmWithIncorrectDate));

        Assertions.assertEquals("Дата релиза должна быть не раньше 28 декабря 1895 года", exception.getMessage());
        Assertions.assertEquals(1, filmController.getAllFilms().size());
    }

    @Test
    public void shouldReturnExceptionIfDurationIsNegative() {
        Film filmWithIncorrectDuration1 = Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(200))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(0)
                .build();
        Film filmWithIncorrectDuration2 = Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(200))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(-1)
                .build();
        ValidationException exception1 = assertThrows(ValidationException.class, () -> filmController.addFilm(filmWithIncorrectDuration1));
        ValidationException exception2 = assertThrows(ValidationException.class, () -> filmController.addFilm(filmWithIncorrectDuration2));

        Assertions.assertEquals("Продолжительность фильма должна быть положительным числом", exception1.getMessage());
        Assertions.assertEquals("Продолжительность фильма должна быть положительным числом", exception2.getMessage());
    }

    @Test
    void shouldCorrectUpdatedFilm() {
        Film oldFilm = Film.builder()
                .name("Интерстеллар")
                .description("a".repeat(200))
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build();
        Film newFilm = Film.builder()
                .name("Время")
                .description("a".repeat(200))
                .releaseDate(LocalDate.of(2020, 1, 23))
                .duration(198)
                .build();

        Film oldFilmWithId = filmController.addFilm(oldFilm);
        newFilm.setId(oldFilmWithId.getId());
        Film updatedFilm = filmController.update(newFilm);

        Assertions.assertEquals(1, filmController.getAllFilms().size());
        Assertions.assertEquals("Время", updatedFilm.getName());
    }
}
