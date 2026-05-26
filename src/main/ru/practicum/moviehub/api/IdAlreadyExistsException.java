package ru.practicum.moviehub.api;

public class IdAlreadyExistsException extends RuntimeException{
    public IdAlreadyExistsException(String message) {
        super(message);
    }
}
