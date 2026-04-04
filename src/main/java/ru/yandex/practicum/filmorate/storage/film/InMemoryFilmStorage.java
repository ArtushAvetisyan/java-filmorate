package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private static final LocalDate FILM_MINIMUM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_SIZE = 200;
    private final Map<Long, Film> movies = new HashMap<>();

    @Override
    public Collection<Film> getAllFilms() {
        return new ArrayList<>(movies.values());
    }

    @Override
    public Film addFilm(Film film) {
        checkFilmReleaseDate(film);
        long id = nextFilmId();
        film.setId(id);
        movies.put(id, film);
        log.info("Добавлен новый фильм: {}", film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!movies.containsKey(film.getId())) {
            log.warn("Не удалось найти фильм c данным ID: {}", film.getId());
            throw new NotFoundException("К сожалению не удалось найти фильм с таким ID");
        }
        Film oldFilm = movies.get(film.getId());
        if (film.getName() != null && !film.getName().isBlank()) {
            oldFilm.setName(film.getName());
            log.info("Название фильма успешно обновлено. Новое название: {}", film.getName());
        }
        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            if (film.getDescription().length() > MAX_DESCRIPTION_SIZE) {
                log.warn("Описание фильма превышает " + MAX_DESCRIPTION_SIZE + " символов");
                throw new ValidationException("Описание не может превышать 200 символов");
            }
            oldFilm.setDescription(film.getDescription());
            log.info("Описание фильма успешно обновлено: ID - {}", film.getId());
        }
        if (film.getReleaseDate() != null && !film.getReleaseDate().equals(oldFilm.getReleaseDate())) {
            checkFilmReleaseDate(film);
            oldFilm.setReleaseDate(film.getReleaseDate());
            log.info("Дата релиза фильма успешно обновлена: ID - {}", film.getId());
        }
        if (film.getDuration() < 0) {
            log.warn("Продолжительность фильма не может быть отрицательным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        } else if (film.getDuration() > 0 && film.getDuration() != oldFilm.getDuration()) {
            oldFilm.setDuration(film.getDuration());
            log.info("Продолжительность фильма успешно обновлена: ID - {}", film.getId());
        }
        return movies.get(film.getId());
    }

    @Override
    public Optional<Film> getFilmById(long id) {
        return Optional.ofNullable(movies.get(id));
    }

    private void checkFilmReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(FILM_MINIMUM_RELEASE_DATE)) {
            log.warn("Дата релиза раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
    }

    private Long nextFilmId() {
        long maxId = movies.values()
                .stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0);
        return ++maxId;
    }

    // Заглушки (побоялся удалять класс, так как такое требование отсутствует)
    @Override
    public void addLike(long filmId, long userId) {
    }

    @Override
    public void deleteLike(long filmId, long userId) {
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        return List.of();
    }
}
