package com.ticketbook.movieapp; // Updated package

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.ticketbook.movieapp.db.DatabaseConnection; // Updated import
import com.ticketbook.movieapp.ui.LoginFrame;       // Updated import
import com.mongodb.MongoException;

/**
 * Main application class to start the Movie Ticket Booking System.
 */
public class App {

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(() -> {
            try {
                // Optional: Set Look and Feel (Nimbus, Metal, System Default)
                // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                 UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception e) {
                System.err.println("Couldn't set Look and Feel: " + e.getMessage());
                // Continue with the default L&F
            }

            try {
                // Attempt to establish database connection early
                DatabaseConnection.getDatabase();
                System.out.println("Database connection established successfully on startup.");

                // Create and show the login frame
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);

            } catch (MongoException me) {
                 System.err.println("FATAL: Could not connect to the database. Application cannot start.");
                 // Display an error message to the user
                 javax.swing.JOptionPane.showMessageDialog(null,
                     "Could not connect to the database.\nPlease check your connection settings and ensure MongoDB is running.\nError: " + me.getMessage(),
                     "Database Connection Error",
                     javax.swing.JOptionPane.ERROR_MESSAGE);
                 // Optionally exit
                 // System.exit(1);
            } catch (Exception e) {
                 System.err.println("An unexpected error occurred during startup: " + e.getMessage());
                 e.printStackTrace(); // Print stack trace for debugging
                 javax.swing.JOptionPane.showMessageDialog(null,
                     "An unexpected error occurred during startup.\nError: " + e.getMessage(),
                     "Application Startup Error",
                     javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add a shutdown hook to close the database connection when the JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Application shutting down, closing database connection...");
            DatabaseConnection.closeConnection();
        }));
    }
}