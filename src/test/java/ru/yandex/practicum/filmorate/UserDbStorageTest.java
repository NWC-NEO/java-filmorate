package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/schema.sql"})
class UserDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        UserMapper userMapper = new UserMapper();
        userStorage = new UserDbStorage(jdbcTemplate, userMapper);
    }

    @Test
    void shouldAddUser() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User saved = userStorage.add(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@mail.ru");
        assertThat(saved.getLogin()).isEqualTo("testuser");
    }

    @Test
    void shouldFindUserById() {
        User user = User.builder()
                .email("find@mail.ru")
                .login("findme")
                .name("Find Me")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        User saved = userStorage.add(user);
        Optional<User> found = userStorage.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("find@mail.ru");
        assertThat(found.get().getName()).isEqualTo("Find Me");
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder()
                .email("old@mail.ru")
                .login("oldlogin")
                .name("Old Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User saved = userStorage.add(user);
        saved.setEmail("new@mail.ru");
        saved.setName("New Name");
        userStorage.update(saved);

        Optional<User> updated = userStorage.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getEmail()).isEqualTo("new@mail.ru");
        assertThat(updated.get().getName()).isEqualTo("New Name");
    }

    @Test
    void shouldFindAllUsers() {
        User user1 = User.builder()
                .email("user1@mail.ru")
                .login("user1")
                .name("User 1")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User user2 = User.builder()
                .email("user2@mail.ru")
                .login("user2")
                .name("User 2")
                .birthday(LocalDate.of(2000, 2, 2))
                .build();

        userStorage.add(user1);
        userStorage.add(user2);

        assertThat(userStorage.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldDeleteUser() {
        User user = User.builder()
                .email("delete@mail.ru")
                .login("deleteme")
                .name("Delete Me")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User saved = userStorage.add(user);
        userStorage.delete(saved.getId());

        assertThat(userStorage.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldHandleUserWithNullName() {
        User user = User.builder()
                .email("noname@mail.ru")
                .login("noname")
                .name(null)
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User saved = userStorage.add(user);

        assertThat(saved.getName()).isNull();
        Optional<User> found = userStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isNull();
    }
}