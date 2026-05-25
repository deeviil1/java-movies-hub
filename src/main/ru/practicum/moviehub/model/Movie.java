package ru.practicum.moviehub.model;

import java.util.Objects;

public class Movie {
    private String title;

    private int years;

    private int id;

    public Movie(String title, int years, int id) {
        this.title = title;
        this.years = years;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitel(String titel) {
        this.title = titel;
    }

    public int getYears() {
        return years;
    }

    public void setYears(int years) {
        this.years = years;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return getYears() == movie.getYears() && getId() == movie.getId() && Objects.equals(getTitle(), movie.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getYears(), getId());
    }
}