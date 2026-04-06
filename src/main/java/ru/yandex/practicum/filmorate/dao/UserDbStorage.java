package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserMapper mapper;

    @Override
    public List<User> getAllUsers() {
        String query = "SELECT * FROM users";
        return jdbc.query(query, mapper);
    }

    @Override
    public Optional<User> getUserById(long id) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        return jdbc.query(query, mapper, id).stream().findFirst();
    }

    @Override
    public User createNewUser(User user) {
        String query = "INSERT INTO users (name, email, login, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, new String[]{"user_id"});
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getLogin());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        String query = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE user_id = ?";
        int rowsUpdated = jdbc.update(query,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        if (rowsUpdated == 0) throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        return user;
    }

    @Override
    public List<User> getUserFriendList(long userId) {
        String query = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbc.query(query, mapper, userId);
    }

    @Override
    public List<User> getCommonFriends(long firstUserId, long secondUserId) {
        String query = "SELECT u.* FROM users AS u " +
                "JOIN friends AS f1 ON u.user_id = f1.friend_id " +
                "JOIN friends AS f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbc.query(query, mapper, firstUserId, secondUserId);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        String query = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbc.update(query, userId, friendId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        String query = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbc.update(query, userId, friendId);
    }
}
