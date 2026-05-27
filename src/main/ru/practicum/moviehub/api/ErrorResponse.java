package ru.practicum.moviehub.api;

public class ErrorResponse {
    private final int status;
    private final String message;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}