package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        boolean isRemoved = userStorage.removeFriend(userId, friendId);

        if (isRemoved) {
            log.info("Пользователь {} удалил из друзей {}", userId, friendId);
        } else {
            log.info("Пользователь {} не имеет в друзьях {}", userId, friendId);
        }
    }

    public List<User> getFriends(Long userId) {
        getUserOrThrow(userId);
        return userStorage.getFriends(userId); // Вызов хранилища
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        getUserOrThrow(id);
        getUserOrThrow(otherId);
        return userStorage.getCommonFriends(id, otherId); // Вызов хранилища
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
