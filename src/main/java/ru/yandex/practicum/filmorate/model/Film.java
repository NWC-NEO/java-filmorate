package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    public static final int MAX_DESCRIPTION_LENGTH = 200;
    private Long id;
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    @Size(max = MAX_DESCRIPTION_LENGTH, message = "Максимальная длина описания - " + MAX_DESCRIPTION_LENGTH)
    private String description;
    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительной")
    private Integer duration;
    @NotNull(message = "MPA рейтинг обязателен")
    private Mpa mpa;
    @Builder.Default
    private List<Genre> genres = new ArrayList<>();
}
