package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Mpa;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MpaDbStorage {
    private final JdbcTemplate jdbc;

    public Mpa getMpaById(long mpaId) {
        try {
            String query = "SELECT * FROM mpa WHERE mpa_id = ?";
            return jdbc.queryForObject(query, mpaMapper, mpaId);
        } catch (EmptyResultDataAccessException exception) {
            log.error("MPA с id - {} не найден", mpaId);
            throw new NotFoundException("MPA с id " + mpaId + " не найден");
        }
    }

    public List<Mpa> getAllMpa() {
        String query = "SELECT * FROM mpa ORDER BY mpa_id";
        return jdbc.query(query, mpaMapper);
    }

    private final RowMapper<Mpa> mpaMapper = ((rs, rowNum) -> {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getLong("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        return mpa;
    });
}
