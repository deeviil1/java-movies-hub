package ru.practicum.moviehub.store;

import ru.practicum.moviehub.api.IdAlreadyExistsException;
import ru.practicum.moviehub.api.MovieAlreadyExistsException;
import ru.practicum.moviehub.api.MovieNotFoundException;
import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MoviesStore {
    private static final int DEFAULT_ID_VALUE = 0;
    private static final int ID_INCREMENT = 1;

    private final List<Movie> movies = new ArrayList<>();
    private int nextId = ID_INCREMENT;

    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies);
    }

    public Movie addMovie(Movie movie) {
        boolean titleExists = movies.stream()
                .anyMatch(existingMovie -> existingMovie.getTitle().equals(movie.getTitle()));

        if (titleExists) {
            throw new MovieAlreadyExistsException("Фильм с таким названием уже есть в списке");
        }

        if (movie.getId() != DEFAULT_ID_VALUE) {
            boolean idExists = movies.stream()
                    .anyMatch(existingMovie -> existingMovie.getId() == movie.getId());
            if (idExists) {
                throw new IdAlreadyExistsException("ID " + movie.getId() + " уже занят");
            }
        }

        if (movie.getId() == DEFAULT_ID_VALUE) {
            movie.setId(nextId);
            nextId += ID_INCREMENT;
        } else {
            if (movie.getId() >= nextId) {
                nextId = movie.getId() + ID_INCREMENT;
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
        Movie movieToDelete = findMovie(id);
        movies.remove(movieToDelete);
    }
}