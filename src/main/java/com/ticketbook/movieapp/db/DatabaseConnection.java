package com.ticketbook.movieapp.db; // Updated package

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoException;

/**
 * Handles the connection to the MongoDB database.
 * Implements the Singleton pattern to ensure only one connection instance.
 */
public class DatabaseConnection {

    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    // --- Configuration ---
    // Replace with your MongoDB connection details
    // Example for local instance: "mongodb://localhost:27017"
    // Example for Atlas: "mongodb+srv://<username>:<password>@<cluster-url>/<database-name>?retryWrites=true&w=majority"
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "movie_booking_db"; // Choose your database name

    // Private constructor to prevent instantiation
    private DatabaseConnection() {}

    /**
     * Establishes a connection to the MongoDB database if not already connected.
     *
     * @return The MongoDatabase instance.
     * @throws MongoException if the connection fails.
     */
    public static MongoDatabase getDatabase() throws MongoException {
        if (mongoClient == null) {
            try {
                System.out.println("Attempting to connect to MongoDB at: " + CONNECTION_STRING);
                // Create a new client and connect to the server
                mongoClient = MongoClients.create(CONNECTION_STRING);

                // Get the database
                database = mongoClient.getDatabase(DATABASE_NAME);

                // Optional: Ping the database to verify connection
                database.runCommand(new org.bson.Document("ping", 1));

                System.out.println("Successfully connected to MongoDB database: " + DATABASE_NAME);

            } catch (MongoException e) {
                System.err.println("Error connecting to MongoDB: " + e.getMessage());
                // Optionally close the client if partially initialized and failed
                closeConnection();
                throw e; // Re-throw the exception to be handled by the caller
            }
        }
        return database;
    }

    /**
     * Closes the MongoDB client connection.
     * Should be called when the application exits.
     */
    public static void closeConnection() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                System.out.println("MongoDB connection closed.");
                mongoClient = null; // Reset client
                database = null;    // Reset database instance
            } catch (Exception e) {
                System.err.println("Error closing MongoDB connection: " + e.getMessage());
            }
        }
    }
}