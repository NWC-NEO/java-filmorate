package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
    }

    @Test
    void shouldAddFriendsMutually() {
        User user = userStorage.add(User.builder().email("u1@mail.ru").login("u1").build());
        User friend = userStorage.add(User.builder().email("u2@mail.ru").login("u2").build());

        userService.addFriend(user.getId(), friend.getId());

        assertTrue(user.getFriends().contains(friend.getId()), "У пользователя должен появиться друг");
        assertTrue(friend.getFriends().contains(user.getId()), "Добавление в друзья должно быть взаимным");
    }

    @Test
    void shouldThrowExceptionWhenAddingNonExistentFriend() {
        User user = userStorage.add(User.builder().email("u1@m.ru").login("u1").build());

        assertThrows(NotFoundException.class, () -> userService.addFriend(user.getId(), 999L));
    }

    @Test
    void shouldReturnCommonFriends() {
        User user1 = userStorage.add(User.builder().email("u1@mail.ru").login("u1").build());
        User user2 = userStorage.add(User.builder().email("u2@mail.ru").login("u2").build());
        User commonFriend = userStorage.add(User.builder().email("cf@mail.ru").login("cf").build());

        userService.addFriend(user1.getId(), commonFriend.getId());
        userService.addFriend(user2.getId(), commonFriend.getId());

        List<User> common = userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, common.size());
        assertEquals(commonFriend.getId(), common.get(0).getId());
    }
}