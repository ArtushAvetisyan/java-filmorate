package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    public void shouldCreateUserIfDataIsCorrect() {
        User user = User.builder()
                .email("abcd@yandex.ru")
                .name("Ivan")
                .login("qwerty")
                .birthday(LocalDate.of(1990, 12, 12))
                .build();
        userController.createNewUser(user);

        Assertions.assertEquals(1, userController.getAllUsers().size());
    }

    @Test
    void shouldReturnAllUsers() {
        User ivan = User.builder()
                .email("ivan@yandex.ru")
                .name("Ivan")
                .login("qwerty1")
                .birthday(LocalDate.of(1990, 12, 12))
                .build();

        User petya = User.builder()
                .email("petr@yandex.ru")
                .name("Petr")
                .login("qwerty2")
                .birthday(LocalDate.of(1993, 2, 1))
                .build();

        userController.createNewUser(ivan);
        userController.createNewUser(petya);
        List<User> usersList = new ArrayList<>(userController.getAllUsers());

        Assertions.assertEquals(2, usersList.size());
        Assertions.assertEquals("Ivan", usersList.getFirst().getName());
        Assertions.assertEquals("Petr", usersList.getLast().getName());
    }

    @Test
    void shouldCorrectChangeEmptyNameToLogin() {
        User userWithSpaceName = User.builder()
                .email("abcd@yandex.ru")
                .name("   ")
                .login("qwerty")
                .birthday(LocalDate.of(1990, 12, 12))
                .build();

        User userWithoutName = User.builder()
                .email("abcd@yandex.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1990, 12, 12))
                .build();

        User changedUser1 = userController.createNewUser(userWithSpaceName);
        User changedUser2 = userController.createNewUser(userWithoutName);

        Assertions.assertEquals("qwerty", changedUser1.getName());
        Assertions.assertEquals("qwerty", changedUser2.getName());
    }

    @Test
    public void shouldReturnExceptionIfEmailIsIncorrect() {
        User userWithoutEmail = User.builder()
                .name("Ivan")
                .login("qwerty")
                .birthday(LocalDate.of(1990, 12, 12))
                .build();

        User userWithEmptyEmail = User.builder()
                .email("")
                .name("Ivan")
                .login("qwerty")
                .birthday(LocalDate.of(1990, 12, 12))
                .build();

        User userEmailWithoutSymbol = User.builder()
                .email("ivanpetrov&yandex.ru")
                .name("Ivan")
                .login("qwerty")
                .birthday(LocalDate.of(1990, 12, 12))
                .build();

        ValidationException exception1 = assertThrows(ValidationException.class, () -> userController.createNewUser(userWithoutEmail));
        ValidationException exception2 = assertThrows(ValidationException.class, () -> userController.createNewUser(userWithEmptyEmail));
        ValidationException exception3 = assertThrows(ValidationException.class, () -> userController.createNewUser(userEmailWithoutSymbol));

        Assertions.assertEquals("Имейл не может быть пустым", exception1.getMessage());
        Assertions.assertEquals("Имейл не может быть пустым", exception2.getMessage());
        Assertions.assertEquals("Имейл должен содержать символ @", exception3.getMessage());
    }

    @Test
    public void shouldReturnExceptionIfLoginIsIncorrect() {
        User userWithEmptyLogin = User.builder()
                .name("Ivan")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.of(1990, 12, 12))
                .build();

        User userWithOnlySpaceLogin = User.builder()
                .name("Ivan")
                .login(" ")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.of(1990, 12, 12))
                .build();

        User userWithIncorrectLogin = User.builder()
                .name("Ivan")
                .login("qwerty ivan")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.of(1990, 12, 12))
                .build();

        ValidationException exception1 = assertThrows(ValidationException.class, () -> userController.createNewUser(userWithEmptyLogin));
        ValidationException exception2 = assertThrows(ValidationException.class, () -> userController.createNewUser(userWithOnlySpaceLogin));
        ValidationException exception3 = assertThrows(ValidationException.class, () -> userController.createNewUser(userWithIncorrectLogin));

        Assertions.assertEquals("Логин не может быть пустым", exception1.getMessage());
        Assertions.assertEquals("Логин не может быть пустым", exception2.getMessage());
        Assertions.assertEquals("Логин не может содержать пробелы", exception3.getMessage());
    }

    @Test
    void shouldReturnExceptionIfBirthdayIsIncorrect() {
        User userWithCorrectBirthday = User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build();

        User userWithIncorrectBirthday = User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        User userWithEmptyBirthday = User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .build();

        userController.createNewUser(userWithCorrectBirthday);
        ValidationException exception1 = assertThrows(ValidationException.class, () -> userController.createNewUser(userWithIncorrectBirthday));
        ValidationException exception2 = assertThrows(ValidationException.class, () -> userController.createNewUser(userWithEmptyBirthday));

        Assertions.assertEquals(1, userController.getAllUsers().size());
        Assertions.assertEquals("Дата рождения не может быть в будущем", exception1.getMessage());
        Assertions.assertEquals("Дата рождения не может быть пустым", exception2.getMessage());
    }
}
