package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Film {
    private static final int MAX_DESCRIPTION_SIZE = 200;
    private long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = MAX_DESCRIPTION_SIZE, message = "Описание не может превышать 200 символов")
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private long duration;
}
