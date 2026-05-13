package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Film add(Film film) {
        film.setId(idCounter++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void delete(Long id) {
        films.remove(id);
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Map<Long, List<Genre>> getGenresByFilmIds(List<Long> filmIds) {
        return filmIds.stream()
                .filter(films::containsKey)
                .collect(Collectors.toMap(
                        id -> id,
                        id -> films.get(id).getGenres()
                ));
    }

    // --- Заглушки для совместимости с обновлённым интерфейсом ---

    @Override
    public void addLike(Long filmId, Long userId) {
        throw new UnsupportedOperationException("Метод addLike не реализован в InMemoryStorage");
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        throw new UnsupportedOperationException("Метод removeLike не реализован в InMemoryStorage");
    }

    @Override
    public List<Film> getPopular(int count) {
        throw new UnsupportedOperationException("Метод getPopular не реализован в InMemoryStorage");
    }
}
