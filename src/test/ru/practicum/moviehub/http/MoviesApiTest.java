package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {
    private static final String BASE_URL = "http://localhost:8080";
    private MoviesServer server;
    private HttpClient client;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        gson = new Gson();
        server = new MoviesServer(new MoviesStore(), 8080);
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    private Movie addMovieAndReturn(Movie movie) throws IOException, InterruptedException {
        String json = gson.toJson(movie);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        return gson.fromJson(response.body(), Movie.class);
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("application/json; charset=UTF-8",
                response.headers().firstValue("Content-Type").orElse(""));

        Type movieListType = new TypeToken<List<Movie>>() {}.getType();
        List<Movie> movies = gson.fromJson(response.body(), movieListType);

        assertNotNull(movies);
        assertTrue(movies.isEmpty());
    }

    @Test
    void postMovie_returnsCreated() throws IOException, InterruptedException {
        Movie movie = new Movie("Новый фильм", 1990, 0); // ID = 0 означает "пусть сервер назначит"
        Movie createdMovie = addMovieAndReturn(movie);

        assertNotNull(createdMovie);
        assertEquals(movie.getTitle(), createdMovie.getTitle());
        assertEquals(movie.getYears(), createdMovie.getYears());
        assertTrue(createdMovie.getId() > 0);
    }

    @Test
    void postDuplicateMovie_returnsConflict() throws IOException, InterruptedException {
        Movie movie = new Movie("Новый фильм", 1990, 0);
        addMovieAndReturn(movie);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(409, response.statusCode());
        assertTrue(response.body().contains("Фильм уже есть в списке"));
    }

    @Test
    void getMovieById_returnsMovie() throws IOException, InterruptedException {
        Movie movie = new Movie("Новый фильм", 1990, 0);
        Movie createdMovie = addMovieAndReturn(movie);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies/" + createdMovie.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Movie fetchedMovie = gson.fromJson(response.body(), Movie.class);
        assertEquals(createdMovie.getId(), fetchedMovie.getId());
        assertEquals(createdMovie.getTitle(), fetchedMovie.getTitle());
        assertEquals(createdMovie.getYears(), fetchedMovie.getYears());
    }

    @Test
    void getNonExistentMovie_returnsNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies/99999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Такого фильма нет в списке"));
    }

    @Test
    void deleteMovie_returnsNoContent() throws IOException, InterruptedException {
        Movie movie = new Movie("Новый фильм", 1990, 0);
        Movie createdMovie = addMovieAndReturn(movie);

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies/" + createdMovie.getId()))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, deleteResponse.statusCode());

        // Проверяем, что фильм действительно удалён
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies/" + createdMovie.getId()))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void getAllMovies_returnsAllAddedMovies() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Первый фильм", 1990, 0);
        Movie movie2 = new Movie("Второй фильм", 1992, 0);

        addMovieAndReturn(movie1);
        addMovieAndReturn(movie2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type movieListType = new TypeToken<List<Movie>>() {}.getType();
        List<Movie> movies = gson.fromJson(response.body(), movieListType);

        assertEquals(2, movies.size());

        boolean hasMovie1 = movies.stream().anyMatch(m -> m.getTitle().equals("Первый фильм"));
        boolean hasMovie2 = movies.stream().anyMatch(m -> m.getTitle().equals("Второй фильм"));

        assertTrue(hasMovie1);
        assertTrue(hasMovie2);
    }
}