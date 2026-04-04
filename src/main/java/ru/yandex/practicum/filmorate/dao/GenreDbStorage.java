package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenreDbStorage {
    private final JdbcTemplate jdbc;
    private final GenreMapper genreMapper;

    public Genre getGenreById(long genreId) {
        try {
            String query = "SELECT * FROM genres WHERE genre_id = ?";
            return jdbc.queryForObject(query, genreMapper, genreId);
        } catch (EmptyResultDataAccessException exception) {
            log.error("Жанр с id - {} не найден", genreId);
            throw new NotFoundException("Жанр с id " + genreId + " не найден");
        }
    }

    public List<Genre> getAllGenres() {
        String query = "SELECT * FROM genres ORDER BY genre_id";
        return jdbc.query(query, genreMapper);
    }
}
