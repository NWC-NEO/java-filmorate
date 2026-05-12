package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film add(Film film) {

        String sqlQuery = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
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
        String sqlQuery = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_rating_id = ? WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден"); // Пункт 4
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);

        return findById(film.getId()).get();
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public Collection<Film> findAll() {
        String sqlQuery = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.id ORDER BY f.id";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sqlQuery = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, id);
        return films.stream().findFirst();
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        List<Genre> uniqueGenres = film.getGenres().stream()
                .distinct()
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, uniqueGenres, uniqueGenres.size(),
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

        String sql = "SELECT fg.film_id, g.id, g.name " +
                "FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id IN (" + inSql + ")";

        Map<Long, List<Genre>> genresMap = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
            genresMap.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        });

        return genresMap;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        java.sql.Date releaseDate = rs.getDate("release_date");

        Film film = Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(releaseDate != null ? releaseDate.toLocalDate() : null)
                .duration(rs.getInt("duration"))
                .mpa(new Mpa(rs.getInt("mpa_rating_id"), rs.getString("mpa_name")))
                .build();

        film.setGenres(getGenresByFilmId(film.getId()));
        return film;
    }

    private List<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.* FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), filmId);
    }
}
