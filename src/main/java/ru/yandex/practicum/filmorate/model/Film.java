package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;
    private MpaRating mpaRating;
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
    @Builder.Default
    private final Set<Long> likes = new HashSet<>();
}
