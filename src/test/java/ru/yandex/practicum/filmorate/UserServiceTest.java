package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
    void shouldSendUnconfirmedFriendRequest() {
        User user1 = userStorage.add(User.builder().email("1@m.ru").login("u1").build());
        User user2 = userStorage.add(User.builder().email("2@m.ru").login("u2").build());

        userService.addFriend(user1.getId(), user2.getId());

        assertEquals(FriendshipStatus.UNCONFIRMED, user1.getFriendships().get(user2.getId()),
                "При первой заявке статус должен быть UNCONFIRMED");
        assertFalse(user2.getFriendships().containsKey(user1.getId()),
                "У второго пользователя не должно быть записи до взаимного действия");
    }

    @Test
    void shouldConfirmFriendshipWhenBothAdded() {
        User user1 = userStorage.add(User.builder().email("1@m.ru").login("u1").build());
        User user2 = userStorage.add(User.builder().email("2@m.ru").login("u2").build());

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user2.getId(), user1.getId());

        assertEquals(FriendshipStatus.CONFIRMED, user1.getFriendships().get(user2.getId()));
        assertEquals(FriendshipStatus.CONFIRMED, user2.getFriendships().get(user1.getId()));
    }
}
