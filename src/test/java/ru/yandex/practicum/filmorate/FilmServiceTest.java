package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService filmService;
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
    }

    @Test
    void shouldAddLikeAndSortByPopularity() {
        filmStorage.add(Film.builder().name("Film 1").build());
        Film film2 = filmStorage.add(Film.builder().name("Film 2").build());
        User user = userStorage.add(User.builder().email("u@m.ru").login("u").build());

        filmService.addLike(film2.getId(), user.getId());

        List<Film> popular = filmService.getPopular(10);

        assertEquals(film2.getId(), popular.get(0).getId(), "Фильм с лайком должен быть выше в рейтинге");
        assertEquals(2, popular.size());
    }

    @Test
    void shouldRemoveLike() {
        Film film = filmStorage.add(Film.builder().name("Film").build());
        User user = userStorage.add(User.builder().email("u@m.ru").login("u").build());

        filmService.addLike(film.getId(), user.getId());
        assertEquals(1, film.getLikes().size());

        filmService.removeLike(film.getId(), user.getId());
        assertEquals(0, film.getLikes().size());
    }

    @Test
    void shouldNotDoubleLikeFromSameUser() {
        Film film = filmStorage.add(Film.builder().name("Film").build());
        User user = userStorage.add(User.builder().email("u@m.ru").login("u").build());

        filmService.addLike(film.getId(), user.getId());
        filmService.addLike(film.getId(), user.getId());

        assertEquals(1, film.getLikes().size(), "Количество лайков не должно увеличиваться при повторе");
    }

    @Test
    void shouldCreateFilmWithGenresAndMpa() {
        Film film = Film.builder()
                .name("New Film")
                .description("Description")
                .releaseDate(LocalDate.now())
                .duration(100)
                .mpaRating(MpaRating.G)
                .genres(Set.of(Genre.ACTION, Genre.COMEDY))
                .build();

        Film created = filmService.create(film);

        assertNotNull(created.getMpaRating());
        assertEquals(MpaRating.G, created.getMpaRating());
        assertEquals(2, created.getGenres().size());
        assertTrue(created.getGenres().contains(Genre.ACTION));
    }
}