package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenreDbStorage {
    private final JdbcTemplate jdbc;
    private final GenreMapper genreMapper;

    public Optional<Genre> getGenreById(long genreId) {
        String query = "SELECT * FROM genres WHERE genre_id = ?";
        List<Genre> genres = jdbc.query(query, genreMapper, genreId);
        return genres.stream().findFirst();
    }

    public List<Genre> getAllGenres() {
        String query = "SELECT * FROM genres ORDER BY genre_id";
        return jdbc.query(query, genreMapper);
    }
}
