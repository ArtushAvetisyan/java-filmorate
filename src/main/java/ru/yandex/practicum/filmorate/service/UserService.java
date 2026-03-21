package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getUsers().values();
    }

    public User getUserById(long id) {
        return Optional.ofNullable(userStorage.getUsers().get(id)).orElseThrow(() -> new NotFoundException
                ("Пользователь с id - " + id + " не найден"));
    }

    public List<User> getUserFriendList(long id) {
        return getUserById(id).getFriends().stream()
                .map(this::getUserById)
                .toList();
    }

    public User create(User user) {
        return userStorage.createNewUser(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void addFriend(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи с id - {} и id - {} добавлены в список друзей", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        friend.getFriends().remove(userId);
        user.getFriends().remove(friendId);
        log.info("Пользователь с id - {} и id - {} удалены из списка друзей", userId, friendId);
    }

    public List<User> getCommonFriends(long firstUserId, long secondUserId) {
        User firstUser = getUserById(firstUserId);
        User secondUser = getUserById(secondUserId);

        return firstUser.getFriends().stream()
                .filter(id -> secondUser.getFriends().contains(id))
                .map(this::getUserById)
                .toList();
    }
}
