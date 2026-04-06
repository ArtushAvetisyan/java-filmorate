package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.dao.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;

import java.time.LocalDate;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({UserDbStorage.class, UserMapper.class})
public class UserDbStorageTest {
    private final UserDbStorage userDbStorage;

    @Test
    void shouldCreateNewUser() {
        User user = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User createdUser = userDbStorage.createNewUser(user);
        Assertions.assertEquals(user, createdUser);
        Assertions.assertEquals(1, createdUser.getId());
        Assertions.assertEquals(1, userDbStorage.getAllUsers().size());
    }

    @Test
    void shouldCorrectReturnAllUsers() {
        User firstUser = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User secondUser = User.builder()
                .name("Пётр")
                .email("Petrov@ya.ru")
                .login("qwerty111")
                .birthday(LocalDate.of(1994, 8, 15))
                .build();

        userDbStorage.createNewUser(firstUser);
        userDbStorage.createNewUser(secondUser);
        List<User> users = userDbStorage.getAllUsers();

        Assertions.assertEquals(2, users.size());
        Assertions.assertEquals(firstUser, users.get(0));
        Assertions.assertEquals(secondUser, users.get(1));
    }

    @Test
    void shouldCorrectReturnUserById() {
        User user = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User createdUser = userDbStorage.createNewUser(user);
        User foundUser = userDbStorage.getUserById(createdUser.getId()).get();
        Assertions.assertEquals(createdUser, foundUser);
    }

    @Test
    void shouldCorrectUpdateUser() {
        User firstUser = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User savedUser = userDbStorage.createNewUser(firstUser);
        Assertions.assertEquals(1, userDbStorage.getAllUsers().size());
        Assertions.assertEquals("Иван", userDbStorage.getAllUsers().getFirst().getName());

        User secondUser = User.builder()
                .id(savedUser.getId())
                .name("Пётр")
                .email("Petrov@ya.ru")
                .login("qwerty111")
                .birthday(LocalDate.of(1994, 8, 15))
                .build();

        User updatedUser = userDbStorage.update(secondUser);
        Assertions.assertEquals(secondUser, updatedUser);
        Assertions.assertEquals(1, userDbStorage.getAllUsers().size());
        Assertions.assertEquals("Petrov@ya.ru", userDbStorage.getAllUsers().getFirst().getEmail());
        Assertions.assertEquals("Пётр", userDbStorage.getAllUsers().getFirst().getName());
    }

    @Test
    void shouldCorrectAddFriend() {
        User firstUser = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User secondUser = User.builder()
                .name("Пётр")
                .email("Petrov@ya.ru")
                .login("qwerty111")
                .birthday(LocalDate.of(1994, 8, 15))
                .build();

        User first = userDbStorage.createNewUser(firstUser);
        User second = userDbStorage.createNewUser(secondUser);
        userDbStorage.addFriend(first.getId(), second.getId());

        List<User> friends = userDbStorage.getUserFriendList(first.getId());
        Assertions.assertEquals(1, friends.size());
        Assertions.assertEquals(second, friends.get(0));
        // Проверка, что дружба односторонняя
        Assertions.assertEquals(0, userDbStorage.getUserFriendList(second.getId()).size());
    }

    @Test
    void shouldCorrectReturnUserFriendList() {
        User firstUser = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User secondUser = User.builder()
                .name("Пётр")
                .email("Petrov@ya.ru")
                .login("qwerty111")
                .birthday(LocalDate.of(1994, 8, 15))
                .build();

        User first = userDbStorage.createNewUser(firstUser);
        User second = userDbStorage.createNewUser(secondUser);
        userDbStorage.addFriend(first.getId(), second.getId());

        List<User> friends = userDbStorage.getUserFriendList(first.getId());
        Assertions.assertEquals(1, friends.size());
        Assertions.assertEquals(second, friends.get(0));
    }

    @Test
    void shouldCorrectDeleteFriend() {
        User firstUser = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User secondUser = User.builder()
                .name("Пётр")
                .email("Petrov@ya.ru")
                .login("qwerty111")
                .birthday(LocalDate.of(1994, 8, 15))
                .build();

        User first = userDbStorage.createNewUser(firstUser);
        User second = userDbStorage.createNewUser(secondUser);
        userDbStorage.deleteFriend(first.getId(), second.getId());

        List<User> friends = userDbStorage.getUserFriendList(first.getId());
        Assertions.assertEquals(0, friends.size());
    }

    @Test
    void shouldReturnCommonFriends() {
        User firstUser = User.builder()
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build();

        User secondUser = User.builder()
                .name("Пётр")
                .email("Petrov@ya.ru")
                .login("qwerty111")
                .birthday(LocalDate.of(1994, 8, 15))
                .build();

        User thirdUser = User.builder()
                .name("Виктория")
                .email("vika@ya.ru")
                .login("qwerty134")
                .birthday(LocalDate.of(1998, 3, 7))
                .build();

        userDbStorage.createNewUser(firstUser);
        userDbStorage.createNewUser(secondUser);
        userDbStorage.createNewUser(thirdUser);
        userDbStorage.addFriend(firstUser.getId(), secondUser.getId());
        userDbStorage.addFriend(firstUser.getId(), thirdUser.getId());
        userDbStorage.addFriend(secondUser.getId(), thirdUser.getId());

        List<User> commonFriends = userDbStorage.getCommonFriends(firstUser.getId(), secondUser.getId());
        Assertions.assertEquals(1, commonFriends.size());
        Assertions.assertEquals(thirdUser, commonFriends.get(0));
    }

    @Test
    void shouldThrowExceptionIfUserNotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> userDbStorage.update(User.builder()
                .id(1)
                .name("Иван")
                .email("ivanov@ya.ru")
                .login("qwerty")
                .birthday(LocalDate.of(1992, 7, 10))
                .build()));
    }

    @Test
    void shouldReturnOptionalEmptyIfUserNotFoundById() {
        Assertions.assertTrue(userDbStorage.getUserById(1).isEmpty());
    }
}
