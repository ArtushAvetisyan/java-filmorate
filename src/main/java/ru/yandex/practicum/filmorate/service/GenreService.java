package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {
    private final GenreDbStorage genreDbStorage;

    public Collection<Genre> getAll() {
        return genreDbStorage.getAllGenres();
    }

    public Genre getGenre(Long id) {
        return genreDbStorage.getGenreById(id).orElseThrow(() -> {
            log.error("Жанр с id {} не найден", id);
            return new NotFoundException("Жанр с id " + id + " не найден");
        });
    }
}
