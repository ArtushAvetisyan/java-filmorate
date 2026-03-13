package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
    private final Map<Long, Film> movies = new HashMap<>();

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        validateFilm(film);
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
            throw new ValidationException("К сожалению не удалось найти фильм с таким ID");
        }
        validateFilm(film);
        movies.put(film.getId(), film);
        log.info("Фильм успешно заменён: ID - {}", film.getId());
        return film;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return movies.values();
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Пустое название фильма");
            throw new ValidationException("Название фильмы не может быть пустым");
        }
        if (film.getDescription() == null) {
            log.warn("Пустое описание фильма");
            throw new ValidationException("Описание фильма не может быть пустым");
        } else if (film.getDescription().length() > 200) {
            log.warn("Описание превышает 200 символов");
            throw new ValidationException("Описание не может превышать 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Дата релиза раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.warn("Продолжительность фильма равна или меньше нуля");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
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
