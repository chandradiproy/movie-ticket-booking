package com.ticketbook.movieapp.models; // Updated package

import org.bson.types.ObjectId;
import java.util.List;
import java.util.ArrayList; // Added import

public class Movie {
    private ObjectId id; // MongoDB _id
    private String title;
    private String description;
    private String genre; // Example extra field
    private int durationMinutes; // Example extra field
    private String rating; // Example: "PG-13"
    private String theatre; // Theatre where it's playing
    private List<String> showtimes; // List of available showtime strings

    // Default constructor (often needed by frameworks/libraries)
    public Movie() {
         this.showtimes = new ArrayList<>(); // Initialize list
    }

    // Parameterized constructor
    public Movie(String title, String description, String genre, int durationMinutes, String rating, String theatre, List<String> showtimes) {
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
        this.theatre = theatre;
        this.showtimes = (showtimes != null) ? showtimes : new ArrayList<>(); // Ensure list is initialized
    }

    // Getters and Setters for all fields
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getTheatre() { return theatre; }
    public void setTheatre(String theatre) { this.theatre = theatre; }

    public List<String> getShowtimes() {
        // Ensure list is never null when accessed
        return (showtimes != null) ? showtimes : new ArrayList<>();
    }
    public void setShowtimes(List<String> showtimes) {
        this.showtimes = showtimes;
    }

    // Override toString() for display in JList or debugging
    @Override
    public String toString() {
        // Customize this for how you want it to appear in the JList
        return title + " (" + (theatre != null ? theatre : "N/A") + ")";
    }
}