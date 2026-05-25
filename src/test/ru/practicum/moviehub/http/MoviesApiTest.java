package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
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
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static Gson gson;

    @BeforeEach
    void beforeEach() throws IOException {
        gson = new Gson();
        server = new MoviesServer(new MoviesStore(), 8080);
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @AfterEach
    void afterEach() {
        server.stop();
    }


    private long addMovie(Movie movie) throws IOException, InterruptedException {
        String json = gson.toJson(movie);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals("\"Фильм успешно добавлен в список\"", resp.body());


        return 1;
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void post_Movie_return_OK() throws IOException, InterruptedException {
        Movie movie = new Movie("Новый фильм", 1990, 1);
        addMovie(movie);

    }

    @Test
    void post_Exists_Movie_returnError() throws IOException, InterruptedException {
        Movie movie = new Movie("Новый фильм", 1990, 1);
        addMovie(movie);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(409, resp.statusCode(), "Попытка добавить дубликат должна вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);
        assertEquals("Фильм уже есть в списке", resp.body());
    }

    @Test
    void getMovie_by_id() throws IOException, InterruptedException {
        Movie expectedMovie = new Movie("Новый фильм", 1990, 1);
        long movieId = addMovie(expectedMovie);

        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + movieId))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        Movie actualMovie = gson.fromJson(resp.body(), Movie.class);
        assertEquals(expectedMovie.getTitle(), actualMovie.getTitle());
        assertEquals(expectedMovie.getYears(), actualMovie.getYears());

        HttpRequest getReqNotExists = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/999"))
                .GET()
                .build();
        resp = client.send(getReqNotExists, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, resp.statusCode(), "Запрос по несуществующему ID должен вернуть 404");
        assertEquals("Такого фильма нет в списке", resp.body());
    }

    @Test
    void deleteMovie_by_id() throws IOException, InterruptedException {
        Movie movie = new Movie("Новый фильм", 1990, 1);
        long movieId = addMovie(movie);

        HttpRequest deleteReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + movieId))
                .DELETE()
                .build();

        HttpResponse<String> resp = client.send(deleteReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, resp.statusCode(), "DELETE должен вернуть 200");
        assertEquals("Фильм успешно удалён", resp.body());
    }

    @Test
    void returnAllMovies() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Первый фильм", 1990, 120);
        Movie movie2 = new Movie("Второй фильм", 1992, 100);

        addMovie(movie1);
        addMovie(movie2);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        // Безопасное парсинг списка фильмов
        Type movieListType = new TypeToken<List<Movie>>(){}.getType();
        List<Movie> actualMovies = gson.fromJson(resp.body(), movieListType);

        assertEquals(2, actualMovies.size());

        boolean movie1Exists = actualMovies.stream()
                .anyMatch(m -> m.getTitle().equals(movie1.getTitle()) &&
                        m.getYears() == movie1.getYears());
        assertTrue(movie1Exists, "Первый фильм должен быть в списке");

        boolean movie2Exists = actualMovies.stream()
                .anyMatch(m -> m.getTitle().equals(movie2.getTitle()) &&
                        m.getYears() == movie2.getYears());
        assertTrue(movie2Exists, "Второй фильм должен быть в списке");
    }
}
