package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbDao implements MpaDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> findAll() {
        return jdbcTemplate.query("SELECT * FROM mpa_ratings ORDER BY id", this::mapRowToMpa);
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        return jdbcTemplate.query(sql, this::mapRowToMpa, id).stream().findFirst();
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("id"), rs.getString("name"));
    }
}
