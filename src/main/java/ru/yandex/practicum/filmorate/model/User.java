package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
public class User {
    private long id;
    private final String email;
    private final String login;
    private String name;
    private final LocalDate birthday;
}
