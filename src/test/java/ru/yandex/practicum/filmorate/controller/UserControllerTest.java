package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {
    private UserController userController;
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        userController = new UserController(new UserService(new InMemoryUserStorage()));
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

        Set<ConstraintViolation<User>> violations1 = validator.validate(userWithoutEmail);
        Set<ConstraintViolation<User>> violations2 = validator.validate(userWithEmptyEmail);
        Set<ConstraintViolation<User>> violations3 = validator.validate(userEmailWithoutSymbol);

        Assertions.assertFalse(violations1.isEmpty());
        Assertions.assertFalse(violations2.isEmpty());
        Assertions.assertFalse(violations3.isEmpty());
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

        Set<ConstraintViolation<User>> violations1 = validator.validate(userWithEmptyLogin);
        Set<ConstraintViolation<User>> violations2 = validator.validate(userWithOnlySpaceLogin);
        Set<ConstraintViolation<User>> violations3 = validator.validate(userWithIncorrectLogin);

        Assertions.assertFalse(violations1.isEmpty());
        Assertions.assertFalse(violations2.isEmpty());
        Assertions.assertFalse(violations3.isEmpty());
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

        Set<ConstraintViolation<User>> violations1 = validator.validate(userWithCorrectBirthday);
        Set<ConstraintViolation<User>> violations2 = validator.validate(userWithIncorrectBirthday);

        Assertions.assertTrue(violations1.isEmpty());
        Assertions.assertFalse(violations2.isEmpty());
    }

    @Test
    void shouldCorrectUpdateFields() {
        User user1 = User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.of(1994, 8, 15))
                .build();

        User user2 = User.builder()
                .id(1)
                .name("Sasha")
                .email("Sasha1994@yandex.ru")
                .build();

        userController.createNewUser(user1);
        userController.update(user2);
        List<User> users = new ArrayList<>(userController.getAllUsers());

        Assertions.assertEquals(1, users.size());
        Assertions.assertEquals("Sasha", users.getFirst().getName());
        Assertions.assertEquals("qwerty", users.getFirst().getLogin());
        Assertions.assertEquals("Sasha1994@yandex.ru", users.getFirst().getEmail());
        Assertions.assertEquals(LocalDate.of(1994, 8, 15), users.getFirst().getBirthday());
    }

    @Test
    void update_shouldThrowException_whenIdIsEmpty() {
        User user = User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userController.update(user));

        Assertions.assertEquals("К сожалению не удалось найти пользователя с таким ID", exception.getMessage());
    }
}