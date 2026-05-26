package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private final HttpServer server;

    public MoviesServer(MoviesStore moviesStore, int port) throws IOException {
        try {
            MoviesHttpHandler moviesHttpHandler = new MoviesHttpHandler(moviesStore);
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/movies", moviesHttpHandler);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось запустить сервер: " + e.getMessage(), e);
        }
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен на порту " + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен");
    }
}