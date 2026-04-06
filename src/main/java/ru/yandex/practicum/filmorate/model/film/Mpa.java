package ru.yandex.practicum.filmorate.model.film;

import lombok.Data;

@Data
public class Mpa {
    private long id;
    private String name;

    public Mpa(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Mpa() {
    }
}
