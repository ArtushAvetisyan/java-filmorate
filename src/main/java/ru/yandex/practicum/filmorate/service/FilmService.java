package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private static final LocalDate FILM_MINIMUM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id).orElseThrow(() -> new NotFoundException(
                "Фильм с id - " + id + " не найден"));
    }

    public Film createFilm(Film film) {
        checkFilmReleaseDate(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        checkFilmReleaseDate(film);
        return filmStorage.update(film);
    }

    public void addLike(long userId, long filmId) {
        // Сначала проверяю, что пользователь и фильм существуют
        userService.getUserById(userId);
        getFilmById(filmId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь с id - {} поставил лайк фильму с id - {}", userId, filmId);
    }

    public void deleteLike(long userId, long filmId) {
        userService.getUserById(userId);
        getFilmById(filmId);
        filmStorage.deleteLike(filmId, userId);
        log.info("Пользователь с id - {} удалил лайк у фильма с id - {}", userId, filmId);
    }

    public List<Film> getMostPopularLikes(int filmLimitCount) {
        if (filmLimitCount <= 0) {
            log.error("Количество фильмов должно быть положительным");
            throw new ValidationException("Количество фильмов должно быть положительным");
        }
        return filmStorage.getMostPopularFilms(filmLimitCount);
    }

    private void checkFilmReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(FILM_MINIMUM_RELEASE_DATE)) {
            log.warn("Дата релиза раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
    }
}
