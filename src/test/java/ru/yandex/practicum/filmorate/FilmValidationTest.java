package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassWhenFilmIsValid() {
        Film film = Film.builder()
                .name("Inception")
                .description("A mind-bending thriller")
                .duration(148)
                .releaseDate(LocalDate.of(2010, 7, 16))
                .build();

        assertTrue(validator.validate(film).isEmpty(), "Валидация должна проходить для корректного фильма");
    }

    @Test
    void shouldFailWhenNameIsEmpty() {
        Film film = Film.builder()
                .name("")
                .description("Description")
                .duration(100)
                .releaseDate(LocalDate.now())
                .build();

        assertFalse(validator.validate(film).isEmpty(), "Пустое название должно вызывать ошибку");
    }

    @Test
    void shouldFailWhenDescriptionIsTooLong() {
        Film film = Film.builder()
                .name("Film")
                .description("a".repeat(201))
                .duration(100)
                .releaseDate(LocalDate.now())
                .build();

        assertFalse(validator.validate(film).isEmpty(), "Описание более 200 символов должно вызывать ошибку");
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        Film film = Film.builder()
                .name("Film")
                .duration(-10)
                .releaseDate(LocalDate.now())
                .build();

        assertFalse(validator.validate(film).isEmpty(), "Отрицательная продолжительность должна вызывать ошибку");
    }
}
