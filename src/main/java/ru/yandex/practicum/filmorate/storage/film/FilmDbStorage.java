package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String INSERT_FILM_SQL = "INSERT INTO films (name, description, release_date, " +
            "duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_FILM_SQL = "UPDATE films SET name = ?, description = ?, " +
            "release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";

    private static final String DELETE_FILM_SQL = "DELETE FROM films WHERE id = ?";

    private static final String FIND_ALL_SQL = "SELECT f.*, m.name AS mpa_name FROM films f " +
            "JOIN mpa_ratings m ON f.mpa_rating_id = m.id ORDER BY f.id";

    private static final String FIND_BY_ID_SQL = "SELECT f.*, m.name AS mpa_name FROM films f " +
            "JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?";

    private static final String DELETE_GENRES_SQL = "DELETE FROM film_genres WHERE film_id = ?";

    private static final String INSERT_GENRE_SQL = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

    private static final String FIND_GENRES_BY_FILM_IDS_SQL = "SELECT fg.film_id, g.id, g.name " +
            "FROM genres g " +
            "JOIN film_genres fg ON g.id = fg.genre_id " +
            "WHERE fg.film_id IN (%s)";

    private static final String FIND_GENRES_BY_ID_SQL = "SELECT g.* FROM genres g " +
            "JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";

    private static final String ADD_LIKE_SQL = "MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)";

    private static final String REMOVE_LIKE_SQL = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    private static final String GET_POPULAR_SQL = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS like_count " +
            "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
            "LEFT JOIN film_likes l ON f.id = l.film_id GROUP BY f.id " +
            "ORDER BY like_count DESC LIMIT ?";

    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;

    @Override
    public Film add(Film film) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_FILM_SQL, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        saveGenres(film);

        log.info("Фильм сохранен в БД с id: {}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {

        int rowsAffected = jdbcTemplate.update(UPDATE_FILM_SQL,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден"); // Пункт 4
        }

        jdbcTemplate.update(DELETE_GENRES_SQL, film.getId());
        saveGenres(film);

        return findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Ошибка при обновлении: фильм с id " + film.getId() + " пропал"));
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(DELETE_FILM_SQL, id);
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_SQL, filmMapper);
        loadGenres(films);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        List<Film> films = jdbcTemplate.query(FIND_BY_ID_SQL, filmMapper, id);
        loadGenres(films);
        return films.stream().findFirst();
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        List<Genre> uniqueGenres = film.getGenres().stream()
                .distinct()
                .toList();

        jdbcTemplate.batchUpdate(INSERT_GENRE_SQL, uniqueGenres, uniqueGenres.size(),
                (ps, genre) -> {
                    ps.setLong(1, film.getId());
                    ps.setInt(2, genre.getId());
                });
    }

    @Override
    public Map<Long, List<Genre>> getGenresByFilmIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Map.of();
        }

        String inSql = filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        String sql = String.format(FIND_GENRES_BY_FILM_IDS_SQL, inSql);

        Map<Long, List<Genre>> genresMap = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
            genresMap.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        });

        return genresMap;
    }

    private void loadGenres(List<Film> films) {
        if (films.isEmpty()) return;
        films.forEach(film -> film.setGenres(getGenresByFilmId(film.getId())));
    }

    private List<Genre> getGenresByFilmId(Long filmId) {
        return jdbcTemplate.query(FIND_GENRES_BY_ID_SQL, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), filmId);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(ADD_LIKE_SQL, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(REMOVE_LIKE_SQL, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        List<Film> films = jdbcTemplate.query(GET_POPULAR_SQL, filmMapper, count);
        loadGenres(films);
        return films;
    }
}
