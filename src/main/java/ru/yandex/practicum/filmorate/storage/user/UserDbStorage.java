package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private static final String INSERT_USER_SQL = "INSERT INTO users (email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?)";

    private static final String UPDATE_USER_SQL = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE id = ?";

    private static final String DELETE_USER_SQL = "DELETE FROM users WHERE id = ?";

    private static final String FIND_ALL_SQL = "SELECT * FROM users";

    private static final String FIND_BY_ID_SQL = "SELECT * FROM users WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User add(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_USER_SQL, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        log.info("Пользователь сохранен в БД с id: {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        int rows = jdbcTemplate.update(UPDATE_USER_SQL, user.getEmail(), user.getLogin(),
                user.getName(), user.getBirthday(), user.getId());
        if (rows == 0) return null;
        return user;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(DELETE_USER_SQL, id);
    }

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, this::mapRowToUser);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, this::mapRowToUser, id).stream().findFirst();
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }
}
