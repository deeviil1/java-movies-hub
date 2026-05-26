package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.api.MovieAlreadyExistsException;
import ru.practicum.moviehub.api.MovieNotFoundException;
import ru.practicum.moviehub.api.idAlreadyExistsException;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MoviesHttpHandler extends BaseHttpHandler {
    private final MoviesStore movieStore;
    private final Gson gson = new Gson();

    public MoviesHttpHandler(MoviesStore movieStore) {
        this.movieStore = movieStore;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().toString();

        try {
            if (method.equalsIgnoreCase("GET")) {
                handleGet(ex, path);
            } else if (method.equalsIgnoreCase("POST")) {
                handlePost(ex, path);
            } else if (method.equalsIgnoreCase("DELETE")) {
                handleDelete(ex, path);
            } else {
                sendJson(ex, 405, gson.toJson(new ErrorResponse(405, "Метод не поддерживается")));
            }
        } catch (idAlreadyExistsException e) {
            sendJson(ex, 409, gson.toJson(new ErrorResponse(409, "Этот ID уже занят")));
        } catch (MovieNotFoundException e) {
            sendJson(ex, 404, gson.toJson(new ErrorResponse(404, "Такого фильма нет в списке")));
        } catch (MovieAlreadyExistsException e) {
            sendJson(ex, 409, gson.toJson(new ErrorResponse(409, "Фильм уже есть в списке")));
        } catch (Exception e) {
            sendJson(ex, 500, gson.toJson(new ErrorResponse(500, "Внутренняя ошибка сервера: " + e.getMessage())));
        }
    }

    private void handleGet(HttpExchange ex, String path) throws IOException {
        if (path.equals("/movies")) {
            sendJson(ex, 200, gson.toJson(movieStore.getAllMovies()));
        } else if (path.matches("/movies/\\d+")) {
            int id = Integer.parseInt(path.split("/")[2]);
            Movie movie = movieStore.findMovie(id);
            sendJson(ex, 200, gson.toJson(movie));
        } else {
            sendJson(ex, 404, gson.toJson(new ErrorResponse(404, "Эндпоинт не найден")));
        }
    }

    private void handlePost(HttpExchange ex, String path) throws IOException {
        if (path.equals("/movies")) {
            String requestBody = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Movie movie = gson.fromJson(requestBody, Movie.class);
            Movie addedMovie = movieStore.addMovie(movie);
            sendJson(ex, 201, gson.toJson(addedMovie));
        } else {
            sendJson(ex, 404, gson.toJson(new ErrorResponse(404, "Эндпоинт не найден")));
        }
    }

    private void handleDelete(HttpExchange ex, String path) throws IOException {
        if (path.matches("/movies/\\d+")) {
            int id = Integer.parseInt(path.split("/")[2]);
            movieStore.deleteMovieById(id);
            sendNoContent(ex);
        } else {
            sendJson(ex, 404, gson.toJson(new ErrorResponse(404, "Эндпоинт не найден")));
        }
    }
}