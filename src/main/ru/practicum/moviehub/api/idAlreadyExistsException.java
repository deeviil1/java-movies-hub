package ru.practicum.moviehub.api;

public class idAlreadyExistsException extends RuntimeException{
    public idAlreadyExistsException(String message) {
        super(message);
    }
}
