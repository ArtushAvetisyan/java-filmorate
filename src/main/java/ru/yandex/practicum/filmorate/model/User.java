package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@Jacksonized
public class User {
    private long id;

    @NotBlank(message = "Имейл не может быть пустым")
    @Email(message = "Некорректный формат имейла. Имейл Обязательно должен содержать символ @")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S*$", message = "Логин не может содержать пробелы")
    private String login;
    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    @Builder.Default
    private Set<Long> friends = new HashSet<>();
}
