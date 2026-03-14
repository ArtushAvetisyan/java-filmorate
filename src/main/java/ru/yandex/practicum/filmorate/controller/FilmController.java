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
    public Film update(@Valid @RequestBody Film film) {
        if (!movies.containsKey(film.getId())) {
            log.warn("Не удалось найти фильм c данным ID: {}", film.getId());
            throw new NotFoundException("К сожалению не удалось найти фильм с таким ID");
        }
        checkFilmReleaseDate(film);
        movies.put(film.getId(), film);
        log.info("Фильм успешно заменён: ID - {}", film.getId());
        return film;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return movies.values();
    }

    private void checkFilmReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(FILM_MINIMUM_RELEASE_DATE)) {
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
