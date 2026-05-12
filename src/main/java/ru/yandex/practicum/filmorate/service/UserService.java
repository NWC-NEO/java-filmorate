package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        // Если друг уже отправил запрос — подтверждаем дружбу
        if (friend.getFriendships().containsKey(userId)) {
            user.getFriendships().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriendships().put(userId, FriendshipStatus.CONFIRMED);
            log.info("Дружба между {} и {} подтверждена", userId, friendId);
        } else {
            // Иначе отправляем неподтверждённый запрос
            user.getFriendships().put(friendId, FriendshipStatus.UNCONFIRMED);
            log.info("Пользователь {} отправил запрос дружбы пользователю {}", userId, friendId);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriendships().remove(friendId);
        friend.getFriendships().remove(userId);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = getUserOrThrow(userId);
        return user.getFriendships().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .map(userStorage::findById)
                .map(opt -> opt.orElseThrow(() -> new NotFoundException("Друг с таким ID не найден в хранилище")))
                .toList();
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);

        Set<Long> commonIds = user.getFriendships().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .filter(friendId -> other.getFriendships().getOrDefault(friendId, FriendshipStatus.UNCONFIRMED)
                        == FriendshipStatus.CONFIRMED)
                .collect(Collectors.toSet());

        return commonIds.stream()
                .map(userStorage::findById)
                .map(opt -> opt.orElseThrow(() -> new NotFoundException("Общий друг не найден")))
                .toList();
    }

    private User getUserOrThrow(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        validateName(user);
        User newUser = userStorage.add(user);
        log.info("Создан новый пользователь: {}", newUser.getLogin());
        return newUser;
    }

    public User update(User user) {
        getUserOrThrow(user.getId());
        validateName(user);
        User updatedUser = userStorage.update(user);
        log.info("Обновлен пользователь: {}", updatedUser.getLogin());
        return updatedUser;
    }

    public User findById(Long id) {
        return getUserOrThrow(id);
    }

    private void validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое, использован логин: {}", user.getLogin());
        }
    }
}
