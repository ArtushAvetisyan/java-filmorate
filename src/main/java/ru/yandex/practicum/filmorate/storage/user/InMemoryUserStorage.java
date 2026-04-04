package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User createNewUser(User user) {
        checkName(user);
        long id = nextUserId();
        user.setId(id);
        users.put(id, user);
        log.info("Пользователь успешно добавлен. Логин: {}", user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("Не удалось найти пользователя: ID - {}", user.getId());
            throw new NotFoundException("К сожалению не удалось найти пользователя с таким ID");
        }
        User oldUser = users.get(user.getId());
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            if (!user.getEmail().contains("@")) {
                log.warn("Email не содержит символ @");
                throw new ValidationException("Email должен содержать символ @");
            }
            oldUser.setEmail(user.getEmail());
            log.info("Email пользователя успешно обновлён. ID - {}", user.getId());
        }
        if (user.getLogin() != null) {
            if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                log.warn("Логин пустой или содержит пробелы");
                throw new ValidationException("Логин не может быть пустым или содержать пробелы");
            }
            oldUser.setLogin(user.getLogin());
            log.info("Логин успешно обновлён. ID - {}", user.getId());
        }
        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Дата рождения не может быть в будущем");
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
            oldUser.setBirthday(user.getBirthday());
            log.info("Дата рождения успешно обновлена. ID - {}", user.getId());
        }
        if (user.getName() != null) {
            oldUser.setName(user.getName());
            log.info("Имя пользователя успешно обновлён. ID - {}", user.getId());
        }
        checkName(oldUser);
        return users.get(user.getId());
    }

    @Override
    public Optional<User> getUserById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.warn("Пустое поле name заменён на login. ID - {}", user.getId());
        }
    }

    private Long nextUserId() {
        long maxId = users.values()
                .stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0);
        return ++maxId;
    }
}
