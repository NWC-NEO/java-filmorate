package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private static final String ADD_FRIEND_SQL = "MERGE INTO friendships (user_id, friend_id) VALUES (?, ?)";
    private static final String REMOVE_FRIEND_SQL = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRIENDS_SQL = "SELECT u.* FROM users u " +
            "JOIN friendships f ON u.id = f.friend_id WHERE f.user_id = ?";
    private static final String GET_COMMON_FRIENDS_SQL = "SELECT u.* FROM users u " +
            "JOIN friendships f1 ON u.id = f1.friend_id " +
            "JOIN friendships f2 ON u.id = f2.friend_id " +
            "WHERE f1.user_id = ? AND f2.user_id = ?";
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        jdbcTemplate.update(ADD_FRIEND_SQL, userId, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        int deleted = jdbcTemplate.update(REMOVE_FRIEND_SQL, userId, friendId);

        if (deleted > 0) {
            log.info("Пользователь {} удалил из друзей {}", userId, friendId);
        } else {
            log.info("Пользователь {} не имеет в друзьях {}", userId, friendId);
        }
    }

    public List<User> getFriends(Long userId) {
        getUserOrThrow(userId);
        return jdbcTemplate.query(GET_FRIENDS_SQL, (rs, rowNum) -> User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build(), userId);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {

        return jdbcTemplate.query(GET_COMMON_FRIENDS_SQL, (rs, rowNum) -> User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build(), id, otherId);
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
