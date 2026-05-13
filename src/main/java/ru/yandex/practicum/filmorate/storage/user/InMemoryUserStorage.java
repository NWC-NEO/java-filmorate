package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public User add(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    // --- Заглушки для совместимости с обновлённым интерфейсом ---

    @Override
    public void addFriend(Long userId, Long friendId) {
        throw new UnsupportedOperationException("Метод addFriend не реализован в InMemoryStorage");
    }

    @Override
    public boolean removeFriend(Long userId, Long friendId) {
        throw new UnsupportedOperationException("Метод removeFriend не реализован в InMemoryStorage");
    }

    @Override
    public List<User> getFriends(Long userId) {
        throw new UnsupportedOperationException("Метод getFriends не реализован в InMemoryStorage");
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        throw new UnsupportedOperationException("Метод getCommonFriends не реализован в InMemoryStorage");
    }
}

