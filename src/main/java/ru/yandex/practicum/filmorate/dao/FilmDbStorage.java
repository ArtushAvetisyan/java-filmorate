package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.dao.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmMapper mapper;
    private final GenreMapper genreMapper;

    @Override
    public List<Film> getAllFilms() {
        String query = "SELECT f.*, m.mpa_id, m.mpa_name " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "ORDER BY f.film_id";

        List<Film> films = jdbc.query(query, mapper);
        if (films.isEmpty()) {
            return films;
        }
        loadGenres(films);
        loadLikes(films);
        return films;
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        String query = "SELECT f.*, m.mpa_name " +
                "FROM films AS f " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "GROUP BY f.film_id, m.mpa_name " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";

        List<Film> films = jdbc.query(query, mapper, count);
        if (films.isEmpty()) {
            return films;
        }
        loadGenres(films);
        loadLikes(films);
        return films;
    }

    @Override
    public Optional<Film> getFilmById(long id) {
        String query = "SELECT f.*,m.mpa_id, m.mpa_name " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";
        Optional<Film> filmOptional = jdbc.query(query, mapper, id).stream().findFirst();
        filmOptional.ifPresent(film -> {
            film.setGenres(getFilmGenres(id));
            film.setLikes(new HashSet<>(getLikesList(id)));
        });
        return filmOptional;
    }

    @Override
    public Film addFilm(Film film) {
        String query = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        setGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String query = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?";
        int rowsUpdated = jdbc.update(query,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        if (rowsUpdated == 0) {
            log.error("Ошибка обновления фильма. Фильм с id - {} не найден", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        setGenres(film);
        return film;
    }

    @Override
    public void addLike(long filmId, long userId) {
        String query = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        int rowUpdated = jdbc.update(query, filmId, userId);
        if (rowUpdated == 0) {
            log.error("Ошибка добавления лайка. Фильм с id - {} не найден", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int rowUpdated = jdbc.update(query, filmId, userId);
        if (rowUpdated == 0) {
            log.error("Лайк от пользователя {} для фильма {} не найден", userId, filmId);
            throw new NotFoundException("Лайк не найден");
        }
    }

    public List<Long> getLikesList(long filmId) {
        String query = "SELECT user_id " +
                "FROM likes " +
                "WHERE film_id = ?";
        return jdbc.query(query, (rs, rowNum) -> rs.getLong("user_id"), filmId);
    }

    private void setGenres(Film film) {
        String deleteOldGenresQuery = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(deleteOldGenresQuery, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String insertQuery = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Genre> genres = film.getGenres().stream().distinct().toList();
        jdbc.batchUpdate(insertQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, film.getId());
                ps.setLong(2, genres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }

    private LinkedHashSet<Genre> getFilmGenres(long filmId) {
        String query = "SELECT g.genre_id, g.name " +
                "FROM genres AS g " +
                "JOIN film_genres AS fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id";
        List<Genre> genres = jdbc.query(query, genreMapper, filmId);
        return new LinkedHashSet<>(genres);
    }

    private void loadGenres(List<Film> films) {
        List<Long> filmIds = films.stream().map(Film::getId).toList();
        String query = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (:ids)";

        SqlParameterSource parameters = new MapSqlParameterSource("ids", filmIds);
        Map<Long, LinkedHashSet<Genre>> genresByFilmId = namedParameterJdbcTemplate.query(query, parameters, rs -> {
            Map<Long, LinkedHashSet<Genre>> map = new HashMap<>();

            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Genre genre = Genre.builder()
                        .id(rs.getLong("genre_id"))
                        .name(rs.getString("name"))
                        .build();
                map.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
            }
            return map;
        });
        if (genresByFilmId == null) {
            genresByFilmId = Collections.emptyMap();
        }
        for (Film film : films) {
            LinkedHashSet<Genre> genres = genresByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>());
            film.setGenres(genres);
        }
    }

    private void loadLikes(List<Film> films) {
        List<Long> filmids = films.stream().map(Film::getId).toList();
        String query = "SELECT film_id, user_id FROM likes WHERE film_id IN (:ids)";
        SqlParameterSource parameters = new MapSqlParameterSource("ids", filmids);
        Map<Long, Set<Long>> likesByFilmId = namedParameterJdbcTemplate.query(query, parameters, rs -> {
            Map<Long, Set<Long>> map = new HashMap<>();

            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Long userId = rs.getLong("user_id");
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
            }
            return map;
        });
        if (likesByFilmId == null) {
            likesByFilmId = Collections.emptyMap();
        }
        for (Film film : films) {
            Set<Long> likes = likesByFilmId.getOrDefault(film.getId(), new HashSet<>());
            film.setLikes(likes);
        }
    }
}


