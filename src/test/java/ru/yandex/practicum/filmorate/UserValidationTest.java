package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassWhenUserIsValid() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("test_login")
                .name("Ivan")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertTrue(validator.validate(user).isEmpty(), "Валидация должна проходить для корректного пользователя");
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        User user = User.builder()
                .email("invalid-email")
                .login("login")
                .birthday(LocalDate.now())
                .build();

        assertFalse(validator.validate(user).isEmpty(), "Email без @ должен вызывать ошибку");
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("login with spaces")
                .build();

        assertFalse(validator.validate(user).isEmpty(), "Логин с пробелами должен вызывать ошибку");
    }

    @Test
    void shouldFailWhenBirthdayInFuture() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("login")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        assertFalse(validator.validate(user).isEmpty(), "Дата рождения в будущем должна вызывать ошибку");
    }
}
