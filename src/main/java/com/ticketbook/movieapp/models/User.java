package com.ticketbook.movieapp.models; // Updated package

import org.bson.types.ObjectId;

public class User {
    private ObjectId id; // Use ObjectId if storing MongoDB's _id
    private String username;
    // Add password hash field if managing users within the app
    // private String passwordHash;
    // Add other fields like email, name, etc.

    // Constructor(s)
    public User(String username /*, String passwordHash */) {
        // this.id = new ObjectId(); // Generate ID on creation if needed
        this.username = username;
        // this.passwordHash = passwordHash;
    }
     // Default constructor (may be needed by some frameworks/libraries)
    public User() {}

    // Getters and Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; } // Needed for MongoDB mapping

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // public String getPasswordHash() { return passwordHash; }
    // public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    // toString() for debugging/logging
    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               '}';
    }
}