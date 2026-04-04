package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(long id) {
        return userStorage.getUserById(id).orElseThrow(() -> new NotFoundException(
                "Пользователь с id - " + id + " не найден"));
    }

    public List<User> getUserFriendList(long id) {
        getUserById(id);
        return userStorage.getUserFriendList(id);
    }

    public User create(User user) {
        if (user == null) {
            log.error("Ошибка при создании пользователя (user == null)");
            throw new ValidationException("Ошибка при создании пользователя ");
        }
        validateName(user);
        return userStorage.createNewUser(user);
    }

    public User update(User user) {
        if (user == null) {
            log.error("Ошибка при обновлении пользователя (user == null)");
            throw new ValidationException("Ошибка при обновлении пользователя");
        }
        validateName(user);
        return userStorage.update(user);
    }

    public void addFriend(long userId, long friendId) {
        // Проверяю, есть ли вообще такие пользователи
        getUserById(userId);
        getUserById(friendId);
        userStorage.addFriend(userId, friendId);
        log.info("Пользователи с id - {} и id - {} добавлены в список друзей", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        getUserById(userId);
        getUserById(friendId);
        userStorage.deleteFriend(userId, friendId);
        log.info("Пользователь с id - {} и id - {} удалены из списка друзей", userId, friendId);
    }

    public List<User> getCommonFriends(long firstUserId, long secondUserId) {
        getUserById(firstUserId);
        getUserById(secondUserId);
        return userStorage.getCommonFriends(firstUserId, secondUserId);
    }

    private void validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
