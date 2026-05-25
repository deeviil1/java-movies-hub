package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.api.MovieAlreadyExistsException;
import ru.practicum.moviehub.api.MovieNotFoundException;
import ru.practicum.moviehub.api.idAlreadyExistsException;
import ru.practicum.moviehub.model.Movie;
import  ru.practicum.moviehub.store.MoviesStore;

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
        System.out.println("Текущий путь: " + path);
        try {
            if (method.equalsIgnoreCase("GET")) {
                if (path.equals("/movies")) {
                    sendJson(ex, 200, gson.toJson(movieStore.getAllMovies()));
                } else if (path.matches("/movies/\\d+")) {
                    int id = Integer.parseInt(path.split("/")[2]);
                    System.out.println("title: " + id);
                    sendJson(ex, 200, gson.toJson(movieStore.findMovie(id)));
                } else {
                    sendJson(ex, 404, new ErrorResponse(404, "Неизвестный эндпоинт").getMessage());
                }
            } else if (method.equalsIgnoreCase("POST")) {
                if (path.equals("/movies")) {
                    String requestBody = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Movie movie = gson.fromJson(requestBody, Movie.class);
                    System.out.println(movie);
                    movieStore.addMovies(movie);
                    sendJson(ex, 201, gson.toJson("Фильм успешно добавлен в список"));
                } else {
                    sendJson(ex, 405, new ErrorResponse(405, "Метод не разрешён для этого пути").getMessage());
                }
            } else if (method.equalsIgnoreCase("DELETE")) {
                if (path.matches("/movies/\\d+")) {
                    int id = Integer.parseInt(path.split("/")[2]);
                    movieStore.deleteMovieById(id);
                    sendJson(ex, 201, "Фильм успешно удалён");
                } else {
                    sendJson(ex, 405, new ErrorResponse(405, "Метод не разрешён для этого пути").getMessage());
                }
            } else {
                sendNoContent(ex);
            }
        } catch (idAlreadyExistsException e) {
            sendJson(ex, 409, new ErrorResponse(409, "Этот id занят").getMessage());
        } catch (MovieNotFoundException e) {
            sendJson(ex, 404, new ErrorResponse(404, "Такого фильма нет в списке").getMessage());
        } catch (MovieAlreadyExistsException e) {
            sendJson(ex, 409, new ErrorResponse(409, "Фильм уже есть в списке").getMessage());
        } catch (Exception e) {
            sendJson(ex, 500, new ErrorResponse(500, e.getMessage()).getMessage());
        }
    }
}
