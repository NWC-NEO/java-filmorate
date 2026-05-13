package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private static final String NOT_FOUND_SUFFIX = " не найден";

    private static final String ADD_LIKE_SQL = "MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)";

    private static final String REMOVE_LIKE_SQL = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    private static final String GET_POPULAR_SQL = "SELECT f.*, m.name AS mpa_name, " +
            "COUNT(l.user_id) AS like_count " +
            "FROM films f " +
            "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
            "LEFT JOIN film_likes l ON f.id = l.film_id " +
            "GROUP BY f.id " +
            "ORDER BY like_count DESC " +
            "LIMIT ?";

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final JdbcTemplate jdbcTemplate;
    private final MpaDao mpaDao;
    private final GenreDao genreDao;

    public void addLike(Long filmId, Long userId) {
        getFilmOrThrow(filmId);
        userService.findById(userId);
        jdbcTemplate.update(ADD_LIKE_SQL, filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(REMOVE_LIKE_SQL, filmId, userId);
    }

    public List<Film> getPopular(int count) {

        List<Film> popularFilms = jdbcTemplate.query(GET_POPULAR_SQL, (rs, rowNum) -> Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(new Mpa(rs.getInt("mpa_rating_id"), rs.getString("mpa_name")))
                .build(), count);

        if (!popularFilms.isEmpty()) {
            List<Long> filmIds = popularFilms.stream()
                    .map(Film::getId)
                    .collect(Collectors.toList());

            Map<Long, List<Genre>> genresMap = filmStorage.getGenresByFilmIds(filmIds);
            popularFilms.forEach(film ->
                    film.setGenres(genresMap.getOrDefault(film.getId(), new ArrayList<>()))
            );
        }

        return popularFilms;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return getFilmOrThrow(id);
    }

    public Film create(Film film) {
        validateFilm(film);

        if (film.getMpa() != null) {
            mpaDao.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA рейтинг с id " + film.getMpa().getId() + NOT_FOUND_SUFFIX));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                genreDao.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + NOT_FOUND_SUFFIX));
            }
        }

        log.info("Добавление фильма: {}", film.getName());
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        validateFilm(film);
        getFilmOrThrow(film.getId());

        if (film.getMpa() != null) {
            mpaDao.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA рейтинг с id " + film.getMpa().getId() + NOT_FOUND_SUFFIX));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                genreDao.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + NOT_FOUND_SUFFIX));
            }
        }

        log.info("Обновление фильма с id: {}", film.getId());
        return filmStorage.update(film);
    }

    private Film getFilmOrThrow(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + NOT_FOUND_SUFFIX));
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Валидация не пройдена: дата релиза фильма раньше {}", CINEMA_BIRTHDAY);
            throw new ValidationException("Дата релиза не может быть раньше " + CINEMA_BIRTHDAY);
        }
    }
}
