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
                new Film("Интерстеллар", "a".repeat(199), LocalDate.of(2014, 11, 7),
                        169),
                new Film("Время", "a".repeat(199), LocalDate.of(2020, 11, 7),
                        170),
                new Film("Звёздные войны", "a".repeat(199), LocalDate.of(2008, 1, 7),
                        200));
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
        Film newFilm = new Film("Интерстеллар", "a".repeat(199), LocalDate.of(2014, 11, 7),
                169);
        Film film = filmController.addFilm(newFilm);

        Assertions.assertEquals(1, filmController.getAllFilms().size());
        Assertions.assertEquals("Интерстеллар", film.getName());
    }

    @Test
    public void shouldReturnExceptionIfNameIsIncorrect() {
        Film newFilm = new Film("", "a".repeat(199), LocalDate.of(2014, 11, 7),
                169);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.addFilm(newFilm));

        Assertions.assertEquals("Название фильмы не может быть пустым", exception.getMessage());
    }

    @Test
    public void shouldReturnExceptionIfDescriptionIsMoreThan200Symbols() {
        Film filmWith201Symbol = new Film("Интерстеллар", "a".repeat(201), LocalDate.of(2014, 11, 7),
                169);
        Film filmWith200Symbols = new Film("Интерстеллар", "a".repeat(200), LocalDate.of(2014, 11, 7),
                169);
        filmController.addFilm(filmWith200Symbols);
        ValidationException exception1 = assertThrows(ValidationException.class, () -> filmController.addFilm(filmWith201Symbol));

        Assertions.assertEquals("Описание не может превышать 200 символов", exception1.getMessage());
        Assertions.assertEquals(1, filmController.getAllFilms().size());
    }

    @Test
    public void shouldReturnExceptionIfReleaseDateIsIncorrect() {
        Film filmWithCorrectDate = new Film("Интерстеллар", "a".repeat(200), LocalDate.of(1895, 12, 28),
                169);
        Film filmWithIncorrectDate = new Film("Интерстеллар", "a".repeat(200), LocalDate.of(1895, 12, 27),
                169);
        filmController.addFilm(filmWithCorrectDate);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.addFilm(filmWithIncorrectDate));

        Assertions.assertEquals("Дата релиза должна быть не раньше 28 декабря 1895 года", exception.getMessage());
        Assertions.assertEquals(1, filmController.getAllFilms().size());
    }

    @Test
    public void shouldReturnExceptionIfDurationIsNegative() {
        Film filmWithIncorrectDuration1 = new Film("Интерстеллар", "a".repeat(200), LocalDate.of(2014, 11, 7),
                0);
        Film filmWithIncorrectDuration2 = new Film("Интерстеллар", "a".repeat(200), LocalDate.of(2014, 11, 7),
                -1);
        ValidationException exception1 = assertThrows(ValidationException.class, () -> filmController.addFilm(filmWithIncorrectDuration1));
        ValidationException exception2 = assertThrows(ValidationException.class, () -> filmController.addFilm(filmWithIncorrectDuration2));

        Assertions.assertEquals("Продолжительность фильма должна быть положительным числом", exception1.getMessage());
        Assertions.assertEquals("Продолжительность фильма должна быть положительным числом", exception2.getMessage());
    }

    @Test
    void shouldCorrectUpdatedFilm() {
        Film oldFilm = new Film("Интерстеллар", "a".repeat(200), LocalDate.of(2014, 11, 7),
                169);
        Film newFilm = new Film("Время", "a".repeat(200), LocalDate.of(2020, 1, 23),
                198);
        // Тут присваиваю одинаковые id
        Film oldFilmWithId = filmController.addFilm(oldFilm);
        newFilm.setId(oldFilmWithId.getId());
        Film updatedFilm = filmController.update(newFilm);

        Assertions.assertEquals(1, filmController.getAllFilms().size());
        Assertions.assertEquals("Время", updatedFilm.getName());
    }
}
