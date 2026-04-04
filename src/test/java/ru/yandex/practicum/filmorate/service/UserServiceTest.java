package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        int incorrectId = 1;
        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> userService.getUserById(incorrectId));

        Assertions.assertEquals("Пользователь с id - " + incorrectId + " не найден", exception.getMessage());
    }

    @Test
    public void shouldReturnUserWhenUserExists() {
        userService.create(User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build());

        User user = userService.getUserById(1);

        Assertions.assertEquals("Ivan", user.getName());
        Assertions.assertEquals("qwerty", user.getLogin());
    }

    @Test
    public void shouldAddFriendSuccessfully() {
        User firstUser = User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build();

        User secondUser = User.builder()
                .name("Andrew")
                .login("qwerty")
                .email("andrew@yandex.ru")
                .birthday(LocalDate.now())
                .build();

        userService.create(firstUser);
        userService.create(secondUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());

        Assertions.assertEquals(1, firstUser.getFriends().size());
        Assertions.assertEquals(1, secondUser.getFriends().size());
    }

    @Test
    public void shouldRemoveFriendSuccessfully() {
        User firstUser = User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build();

        User secondUser = User.builder()
                .name("Andrew")
                .login("qwerty")
                .email("andrew@yandex.ru")
                .birthday(LocalDate.now())
                .build();

        userService.create(firstUser);
        userService.create(secondUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());

        Assertions.assertEquals(1, firstUser.getFriends().size());
        Assertions.assertEquals(1, secondUser.getFriends().size());

        userService.deleteFriend(firstUser.getId(), secondUser.getId());
        Assertions.assertEquals(0, firstUser.getFriends().size());
        Assertions.assertEquals(0, secondUser.getFriends().size());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenAddingNonExistentFriend() {
        int incorrectId = 2;
        User firstUser = User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build();

        userService.create(firstUser);
        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                userService.addFriend(firstUser.getId(), incorrectId));

        Assertions.assertEquals("Пользователь с id - " + incorrectId + " не найден", exception.getMessage());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenDeletingNonExistentFriend() {
        int incorrectId = 2;
        User firstUser = User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build();

        userService.create(firstUser);
        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                userService.deleteFriend(firstUser.getId(), incorrectId));

        Assertions.assertEquals("Пользователь с id - " + incorrectId + " не найден", exception.getMessage());
    }

    @Test
    public void shouldReturnFriendListCorrectly() {
        User firstUser = User.builder()
                .name("Ivan")
                .login("qwerty")
                .email("ivanpetrov@yandex.ru")
                .birthday(LocalDate.now())
                .build();

        User secondUser = User.builder()
                .name("Andrew")
                .login("qwerty")
                .email("andrew@yandex.ru")
                .birthday(LocalDate.now())
                .build();

        userService.create(firstUser);
        userService.create(secondUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());

        List<User> firstUsersFriendList = userService.getUserFriendList(firstUser.getId());
        List<User> secondUsersFriendList = userService.getUserFriendList(secondUser.getId());

        Assertions.assertEquals(1, firstUsersFriendList.size());
        Assertions.assertEquals("Andrew", firstUsersFriendList.getFirst().getName());
        Assertions.assertEquals(1, secondUsersFriendList.size());
        Assertions.assertEquals("Ivan", secondUsersFriendList.getFirst().getName());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenUserDoesNotExistWhileGettingFriends() {
        int incorrectId = 1;

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                userService.getUserFriendList(incorrectId));

        Assertions.assertEquals("Пользователь с id - " + incorrectId + " не найден", exception.getMessage());
    }

    @Test
    public void shouldReturnCommonFriendsSuccessfully() {
        User firstUser = userService.create(User.builder()
                .name("Ivan")
                .login("qwerty1")
                .build());

        User secondUser = userService.create(User.builder()
                .name("Andrew")
                .login("qwerty2")
                .build());

        User commonFriend = userService.create(User.builder()
                .name("Petr")
                .login("qwerty3")
                .build());

        userService.addFriend(firstUser.getId(), commonFriend.getId());
        userService.addFriend(secondUser.getId(), commonFriend.getId());

        List<User> commonFriendList = userService.getCommonFriends(firstUser.getId(), secondUser.getId());

        Assertions.assertEquals(1, commonFriendList.size());
        Assertions.assertEquals("Petr", commonFriendList.getFirst().getName());
    }

    @Test
    void shouldReturnEmptyListWhenNoCommonFriendsExist() {
        User firstUser = userService.create(User.builder()
                .name("Ivan")
                .login("qwerty1")
                .build());

        User secondUser = userService.create(User.builder()
                .name("Andrew")
                .login("qwerty2")
                .build());

        User thirdUser = userService.create(User.builder()
                .name("Petr")
                .login("qwerty3")
                .build());

        userService.addFriend(firstUser.getId(), thirdUser.getId());
        List<User> commonFriends = userService.getCommonFriends(firstUser.getId(), secondUser.getId());

        Assertions.assertTrue(commonFriends.isEmpty());
    }
}
