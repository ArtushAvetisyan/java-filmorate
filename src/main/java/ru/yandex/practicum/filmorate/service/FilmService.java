package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getMovies().values();
    }

    public Film getFilmById(long id) {
        return Optional.ofNullable(filmStorage.getMovies().get(id)).orElseThrow(() -> new NotFoundException
                ("Фильм с id - " + id + " не найден"));
    }

    public Film createFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.update(film);
    }

    public void addLike(long userId, long filmId) {
        Film film = getFilmById(filmId);
        User user = userService.getUserById(userId);
        film.getLikes().add(user.getId());
        log.info("Пользователь с id - {} поставил лайк фильму с id - {}", userId, filmId);
    }

    public void deleteLike(long userId, long filmId) {
        Film film = getFilmById(filmId);
        User user = userService.getUserById(userId);
        film.getLikes().remove(user.getId());
        log.info("Пользователь с id - {} удалил лайк у фильма с id - {}", userId, filmId);
    }

    public List<Film> getMostPopularLikes(int filmLimitCount) {
        return getAllFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(filmLimitCount)
                .toList();
    }
}
