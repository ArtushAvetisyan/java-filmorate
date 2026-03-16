package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private static final LocalDate FILM_MINIMUM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_SIZE = 200;
    private final Map<Long, Film> movies = new HashMap<>();

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        checkFilmReleaseDate(film);
        long id = nextFilmId();
        film.setId(id);
        movies.put(id, film);
        log.info("Добавлен новый фильм: {}", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (!movies.containsKey(film.getId())) {
            log.warn("Не удалось найти фильм c данным ID: {}", film.getId());
            throw new NotFoundException("К сожалению не удалось найти фильм с таким ID");
        }
        Film oldFilm = movies.get(film.getId());
        if (film.getName() != null && !film.getName().isBlank()) {
            oldFilm.setName(film.getName());
            log.info("Название фильма успешно обновлено. Новое название: {}", film.getName());
        }
        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            if (film.getDescription().length() > MAX_DESCRIPTION_SIZE) {
                log.warn("Описание фильма превышает " + MAX_DESCRIPTION_SIZE + " символов");
                throw new ValidationException("Описание не может превышать 200 символов");
            }
            oldFilm.setDescription(film.getDescription());
            log.info("Описание фильма успешно обновлено: ID - {}", film.getId());
        }
        if (film.getReleaseDate() != null && !film.getReleaseDate().equals(oldFilm.getReleaseDate())) {
            checkFilmReleaseDate(film);
            oldFilm.setReleaseDate(film.getReleaseDate());
            log.info("Дата релиза фильма успешно обновлена: ID - {}", film.getId());
        }
        if (film.getDuration() < 0) {
            log.warn("Продолжительность фильма не может быть отрицательным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        } else if (film.getDuration() > 0 && film.getDuration() != oldFilm.getDuration()) {
            oldFilm.setDuration(film.getDuration());
            log.info("Продолжительность фильма успешно обновлена: ID - {}", film.getId());
        }
        return movies.get(film.getId());
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return movies.values();
    }

    private void checkFilmReleaseDate(Film film) {
        // В методе add на null уже есть проверка, но данную проверку оставил, чтобы использовать в update
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(FILM_MINIMUM_RELEASE_DATE)) {
            log.warn("Дата релиза раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
    }

    private Long nextFilmId() {
        long maxId = movies.values()
                .stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0);
        return ++maxId;
    }
}
