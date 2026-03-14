package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User createNewUser(@Valid @RequestBody User user) {
        checkName(user);
        long id = nextUserId();
        user.setId(id);
        users.put(id, user);
        log.info("Пользователь успешно добавлен. Логин: {}", user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("Не удалось найти пользователя: ID - {}", user.getId());
            throw new NotFoundException("К сожалению не удалось найти пользователя с таким ID");
        }
        checkName(user);
        users.put(user.getId(), user);
        log.info("Пользователь успешно заменён: Логин - {}", user.getLogin());
        return user;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.warn("Пустое поле name заменён на login");
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
