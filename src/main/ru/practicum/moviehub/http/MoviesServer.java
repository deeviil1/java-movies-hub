package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private static final int DEFAULT_BACKLOG = 0;
    private static final int STOP_DELAY_SECONDS = 0;
    private static final String MOVIES_CONTEXT_PATH = "/movies";

    private final HttpServer server;

    public MoviesServer(MoviesStore moviesStore, int port) throws IOException {
        try {
            MoviesHttpHandler moviesHttpHandler = new MoviesHttpHandler(moviesStore);
            server = HttpServer.create(new InetSocketAddress(port), DEFAULT_BACKLOG);
            server.createContext(MOVIES_CONTEXT_PATH, moviesHttpHandler);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось запустить сервер: " + e.getMessage(), e);
        }
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен на порту " + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(STOP_DELAY_SECONDS);
        System.out.println("Сервер остановлен");
    }
}