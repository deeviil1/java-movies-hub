package ru.practicum.moviehub.store;


import ru.practicum.moviehub.api.MovieAlreadyExistsException;
import ru.practicum.moviehub.api.MovieNotFoundException;
import ru.practicum.moviehub.api.idAlreadyExistsException;
import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoviesStore {
    private final List<Movie> movies = new ArrayList<>();
    private int nextId = 1;

    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies); // Возвращаем копию для безопасности
    }

    public Movie addMovie(Movie movie) {
        // Проверка дубликата по названию
        boolean titleExists = movies.stream()
                .anyMatch(existingMovie -> existingMovie.getTitle().equals(movie.getTitle()));

        if (titleExists) {
            throw new MovieAlreadyExistsException("Фильм с таким названием уже есть в списке");
        }

        // Если ID передан и уже существует - ошибка
        if (movie.getId() != 0) {
            boolean idExists = movies.stream()
                    .anyMatch(existingMovie -> existingMovie.getId() == movie.getId());
            if (idExists) {
                throw new idAlreadyExistsException("ID " + movie.getId() + " уже занят");
            }
        }

        // Устанавливаем ID (если не задан или равен 0)
        if (movie.getId() == 0) {
            movie.setId(nextId++);
        } else {
            // Обновляем nextId, если переданный ID больше текущего
            if (movie.getId() >= nextId) {
                nextId = movie.getId() + 1;
            }
        }

        movies.add(movie);
        return movie;
    }

    public Movie findMovie(int id) {
        return movies.stream()
                .filter(movie -> movie.getId() == id)
                .findFirst()
                .orElseThrow(() -> new MovieNotFoundException("Фильма с ID " + id + " нет в списке"));
    }

    public void deleteMovieById(int id) {
        Movie movieToDelete = findMovie(id); // Используем findMovie для проверки существования
        movies.remove(movieToDelete);
    }
}