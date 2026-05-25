package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private final HttpServer server;


    public MoviesServer(MoviesStore moviesStore, int i) throws IOException {
        try {
            MoviesStore store = new MoviesStore();
            MoviesHttpHandler moviesHttpHandler = new MoviesHttpHandler(store);
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/movies", moviesHttpHandler);
        }catch (IOException e){
            throw new RuntimeException("Не удалось запустить сервер: " + e.getMessage());
        }

    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен");
    }

    public void start() {
        server.start();
        System.out.println("сервер запущен");
    }
}