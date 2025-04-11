package com.ticketbook.movieapp.models; // Updated package

import org.bson.types.ObjectId;
import java.util.Date; // Or use java.time classes like LocalTime/LocalDateTime if preferred
import java.text.SimpleDateFormat; // Added for formatting in toString

/**
 * Represents a specific showtime for a movie at a theatre.
 * This can be used if showtimes are stored as separate documents
 * or need more detailed information than just a time string.
 */
public class Showtime {

    private ObjectId id;          // Unique identifier for the showtime document in MongoDB
    private ObjectId movieId;     // Reference to the Movie document (_id)
    private String theatre;       // Theatre name (or could be a theatreId)
    private String screen;        // Screen number or name (e.g., "Screen 5", "IMAX")
    private Date startTime;       // The actual date and time of the show start
    // Alternatively, use separate Date and String/LocalTime for easier querying/display:
    // private Date showDate;
    // private String timeString; // e.g., "10:00 AM"

    // Constructors
    public Showtime() {}

    public Showtime(ObjectId movieId, String theatre, String screen, Date startTime) {
        this.movieId = movieId;
        this.theatre = theatre;
        this.screen = screen;
        this.startTime = startTime;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getMovieId() {
        return movieId;
    }

    public void setMovieId(ObjectId movieId) {
        this.movieId = movieId;
    }

    public String getTheatre() {
        return theatre;
    }

    public void setTheatre(String theatre) {
        this.theatre = theatre;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Provides a simple string representation, often used for display.
     * You might want to format the Date object nicely here.
     * Example using SimpleDateFormat (add import java.text.SimpleDateFormat;):
     * SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a"); // e.g., 10:30 AM
     * return sdf.format(startTime);
     */
    @Override
    public String toString() {
        String formattedTime = "N/A";
        if (startTime != null) {
             // Example formatting: "Apr 11, 2025 10:05 PM" or just "10:05 PM"
             SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a"); // Adjust format as needed
             formattedTime = sdf.format(startTime);
        }
        return formattedTime + (screen != null ? " (Screen: " + screen + ")" : "");
    }
}