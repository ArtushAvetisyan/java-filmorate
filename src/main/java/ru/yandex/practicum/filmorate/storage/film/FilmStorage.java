package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Map;

public interface FilmStorage {

    Map<Long, Film> getMovies();

    Film addFilm(Film film);

    Film update(Film film);
}
