package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

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
                .mpa(new Mpa(1, "G"))
                .genres(new ArrayList<>())
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
                .mpa(new Mpa(1, "G"))
                .genres(new ArrayList<>())
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
                .mpa(new Mpa(1, "G"))
                .genres(new ArrayList<>())
                .build();

        assertFalse(validator.validate(film).isEmpty(), "Описание более 200 символов должно вызывать ошибку");
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        Film film = Film.builder()
                .name("Film")
                .duration(-10)
                .releaseDate(LocalDate.now())
                .mpa(new Mpa(1, "G"))
                .genres(new ArrayList<>())
                .build();

        assertFalse(validator.validate(film).isEmpty(), "Отрицательная продолжительность должна вызывать ошибку");
    }

    @Test
    void shouldFailWhenDurationIsZero() {
        Film film = Film.builder()
                .name("Film")
                .duration(0)
                .releaseDate(LocalDate.now())
                .mpa(new Mpa(1, "G"))
                .genres(new ArrayList<>())
                .build();

        assertFalse(validator.validate(film).isEmpty(), "Нулевая продолжительность должна вызывать ошибку (@Positive)");
    }

    @Test
    void shouldFailWhenMpaIsNull() {
        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .duration(100)
                .releaseDate(LocalDate.now())
                .mpa(null)
                .genres(new ArrayList<>())
                .build();

        assertFalse(validator.validate(film).isEmpty(), "Отсутствие MPA рейтинга должно вызывать ошибку (@NotNull)");
    }

    @Test
    void shouldFailWhenReleaseDateIsNull() {
        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .duration(100)
                .releaseDate(null)
                .mpa(new Mpa(1, "G"))
                .genres(new ArrayList<>())
                .build();

        assertFalse(validator.validate(film).isEmpty(), "Отсутствие даты релиза должно вызывать ошибку (@NotNull)");
    }
}