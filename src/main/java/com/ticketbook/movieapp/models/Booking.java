package com.ticketbook.movieapp.models; // Updated package

import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;
import java.util.ArrayList; // Added import

public class Booking {
    private ObjectId id;          // MongoDB _id
    private ObjectId userId;      // Reference to the User (_id)
    private ObjectId movieId;     // Reference to the Movie (_id)
    private String movieTitle;    // Denormalized for easy display
    private String theatre;       // Denormalized
    private String showtime;
    private List<String> seats;   // List of booked seat IDs (e.g., ["A1", "A2"])
    private double totalAmount;
    private Date bookingTime;   // Timestamp of the booking

    // Constructors
    public Booking() {
        this.seats = new ArrayList<>(); // Initialize list
    }

    public Booking(ObjectId userId, ObjectId movieId, String movieTitle, String theatre, String showtime, List<String> seats, double totalAmount, Date bookingTime) {
        this.userId = userId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.theatre = theatre;
        this.showtime = showtime;
        this.seats = (seats != null) ? seats : new ArrayList<>(); // Ensure list is initialized
        this.totalAmount = totalAmount;
        this.bookingTime = bookingTime;
    }

    // Getters and Setters for all fields
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getUserId() { return userId; }
    public void setUserId(ObjectId userId) { this.userId = userId; }

    public ObjectId getMovieId() { return movieId; }
    public void setMovieId(ObjectId movieId) { this.movieId = movieId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getTheatre() { return theatre; }
    public void setTheatre(String theatre) { this.theatre = theatre; }

    public String getShowtime() { return showtime; }
    public void setShowtime(String showtime) { this.showtime = showtime; }

    public List<String> getSeats() {
        return (seats != null) ? seats : new ArrayList<>();
    }
    public void setSeats(List<String> seats) { this.seats = seats; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public Date getBookingTime() { return bookingTime; }
    public void setBookingTime(Date bookingTime) { this.bookingTime = bookingTime; }

    @Override
    public String toString() {
        return "Booking{" +
               "id=" + id +
               ", userId=" + userId +
               ", movieId=" + movieId +
               ", showtime='" + showtime + '\'' +
               ", seats=" + seats +
               ", bookingTime=" + bookingTime +
               '}';
    }
}