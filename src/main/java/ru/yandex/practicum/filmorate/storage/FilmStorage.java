package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> getAllFilms();

    Film addFilm(Film film);

    Film update(Film film);

    Optional<Film> getFilmById(long id);

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    List<Film> getMostPopularFilms(int count);
}