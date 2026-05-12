package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dao.impl.GenreDbDao;
import ru.yandex.practicum.filmorate.dao.impl.MpaDbDao;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/schema.sql", "/data.sql"})
class FilmDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private FilmDbStorage filmStorage;
    private MpaDbDao mpaDao;
    private GenreDbDao genreDao;

    @BeforeEach
    void setUp() {
        mpaDao = new MpaDbDao(jdbcTemplate);
        genreDao = new GenreDbDao(jdbcTemplate);
        filmStorage = new FilmDbStorage(jdbcTemplate);
    }

    @Test
    void shouldAddFilm() {
        Mpa mpa = new Mpa(1, "G");
        Film film = Film.builder()
                .name("Test Film")
                .description("Description")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(new ArrayList<>())
                .build();

        Film saved = filmStorage.add(film);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Film");
    }

    @Test
    void shouldFindFilmById() {
        Mpa mpa = new Mpa(1, "G");
        Film film = Film.builder()
                .name("Find Me")
                .description("Test")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(90)
                .mpa(mpa)
                .genres(new ArrayList<>())
                .build();

        Film saved = filmStorage.add(film);
        Optional<Film> found = filmStorage.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Find Me");
        assertThat(found.get().getMpa().getId()).isEqualTo(1);
    }

    @Test
    void shouldUpdateFilm() {
        Mpa mpa = new Mpa(1, "G");
        Film film = Film.builder()
                .name("Original")
                .description("Test")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(90)
                .mpa(mpa)
                .genres(new ArrayList<>())
                .build();

        Film saved = filmStorage.add(film);
        saved.setName("Updated");
        saved.setDescription("New desc");

        filmStorage.update(saved);
        Optional<Film> updated = filmStorage.findById(saved.getId());

        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated");
        assertThat(updated.get().getDescription()).isEqualTo("New desc");
    }

    @Test
    void shouldFindAllFilms() {
        Mpa mpa = new Mpa(1, "G");
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Test")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(90)
                .mpa(mpa)
                .genres(new ArrayList<>())
                .build();
        Film film2 = Film.builder()
                .name("Film 2")
                .description("Test")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(90)
                .mpa(mpa)
                .genres(new ArrayList<>())
                .build();

        filmStorage.add(film1);
        filmStorage.add(film2);

        assertThat(filmStorage.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldDeleteFilm() {
        Mpa mpa = new Mpa(1, "G");
        Film film = Film.builder()
                .name("To Delete")
                .description("Test")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(90)
                .mpa(mpa)
                .genres(new ArrayList<>())
                .build();

        Film saved = filmStorage.add(film);
        filmStorage.delete(saved.getId());

        assertThat(filmStorage.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldSaveAndRetrieveGenres() {
        Mpa mpa = new Mpa(1, "G");
        Genre genre1 = new Genre(1, "Комедия");
        Genre genre2 = new Genre(2, "Драма");
        List<Genre> genres = new ArrayList<>();
        genres.add(genre1);
        genres.add(genre2);

        Film film = Film.builder()
                .name("Film with genres")
                .description("Test")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(genres)
                .build();

        Film saved = filmStorage.add(film);
        Optional<Film> found = filmStorage.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getGenres()).hasSize(2);
        assertThat(found.get().getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(1, 2);
    }
}
