package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User createNewUser(@RequestBody User user) {
        validate(user);
        long id = nextUserId();
        user.setId(id);
        users.put(id, user);
        log.info("Пользователь успешно добавлен. Логин: {}", user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("Не удалось найти пользователя: ID - {}", user.getId());
            throw new ValidationException("К сожалению не удалось найти пользователя с таким ID");
        }
        validate(user);
        users.put(user.getId(), user);
        log.info("Пользователь успешно заменён: Логин - {}", user.getLogin());
        return user;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Пустое поле email пользователя");
            throw new ValidationException("Имейл не может быть пустым");
        } else if (!user.getEmail().contains("@")) {
            log.warn("Имейл не содержит @");
            throw new ValidationException("Имейл должен содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Пустое поле login пользователя");
            throw new ValidationException("Логин не может быть пустым");
        } else if (user.getLogin().contains(" ")) {
            log.warn("Логин пользователя содержит пробелы");
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Пустое поле name заменён на login");
        }
        if (user.getBirthday() == null) {
            log.warn("Пустое поле birthday");
            throw new ValidationException("Дата рождения не может быть пустым");
        } else if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения указано в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
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
