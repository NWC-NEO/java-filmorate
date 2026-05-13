package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbDao implements GenreDao {
    private static final String FIND_ALL_SQL = "SELECT * FROM genres ORDER BY id";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM genres WHERE id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, this::mapRowToGenre, id).stream().findFirst();
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("id"), rs.getString("name"));
    }
}
