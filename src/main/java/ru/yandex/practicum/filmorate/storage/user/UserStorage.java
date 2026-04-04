package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {

    Collection<User> getAllUsers();

    User createNewUser(User user);

    User update(User user);

    Optional<User> getUserById(long userId);

    List<User> getUserFriendList(long userId);

    List<User> getCommonFriends(long firstUserId, long secondUserId);

    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);
}
