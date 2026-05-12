package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Set;

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

    @Test
    void shouldCreateFilmWithGenresAndMpaRating() {
        Film film = Film.builder()
                .name("Film with genres")
                .description("Description")
                .duration(120)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .genres(Set.of(Genre.COMEDY, Genre.DRAMA))
                .mpaRating(MpaRating.PG_13)
                .build();

        assertTrue(validator.validate(film).isEmpty(), "Фильм с жанрами и рейтингом должен проходить валидацию");
        assertEquals(2, film.getGenres().size(), "Должно быть 2 жанра");
        assertEquals(MpaRating.PG_13, film.getMpaRating(), "Рейтинг должен быть PG-13");
    }

    @Test
    void shouldCreateFilmWithEmptyGenresAndNoMpaRating() {
        Film film = Film.builder()
                .name("Simple Film")
                .description("Description")
                .duration(90)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .build();

        assertTrue(validator.validate(film).isEmpty(), "Фильм без жанров и рейтинга должен проходить валидацию");
        assertTrue(film.getGenres().isEmpty(), "Genres должны быть пустыми по умолчанию");
        assertNull(film.getMpaRating(), "MpaRating должен быть null по умолчанию");
    }
}
