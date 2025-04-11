package com.ticketbook.movieapp.ui; // Updated package

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A simple utility class to display booking confirmation details using JOptionPane.
 */
public class ConfirmationDialog {

    /**
     * Static method to create and show the confirmation dialog.
     *
     * @param parentComponent The component relative to which the dialog should appear (can be null).
     * @param movieTitle      The title of the booked movie.
     * @param showtime        The showtime of the booking.
     * @param seats           The list of booked seats.
     * @param totalAmount     The total amount paid.
     * @param bookingId       The unique ID of the booking (as a String).
     */
    public static void showConfirmation(Component parentComponent,
                                        String movieTitle,
                                        String showtime,
                                        List<String> seats,
                                        double totalAmount,
                                        String bookingId) {

        // Ensure seats list is not null for display
        String seatsString = (seats != null) ? String.join(", ", seats) : "N/A";
        int numTickets = (seats != null) ? seats.size() : 0;

        // Use HTML for better formatting within the JOptionPane message
        String message = String.format(
            "<html><div style='width: 300px; padding: 10px;'>" + // Control width and add padding
            "<h2 style='color: green;'>Booking Confirmed!</h2>" +
            "Thank you for your booking.<br><br>" +
            "<b>Booking ID:</b> %s<br>" +
            "<b>Movie:</b> %s<br>" +
            "<b>Showtime:</b> %s<br>" +
            "<b>Seats:</b> %s<br>" +
            "<b>Tickets:</b> %d<br>" +
            "<b>Total Paid:</b> ₹%.2f<br><br>" + // Use Rupee symbol
            "<hr>" + // Add a separator line
            "<p style='text-align:center;'>Enjoy the show!</p>" +
            "</div></html>",
            (bookingId != null) ? bookingId : "N/A", // Handle null booking ID
            (movieTitle != null) ? movieTitle : "N/A",
            (showtime != null) ? showtime : "N/A",
            seatsString,
            numTickets,
            totalAmount
        );

        // Use JOptionPane for a simple standard dialog
        // Ensure this runs on the Event Dispatch Thread if called from a background thread
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                parentComponent, // Parent (can be null to center on screen)
                message,         // Message (HTML formatted)
                "Booking Confirmation", // Title
                JOptionPane.INFORMATION_MESSAGE // Message type icon
            );
        });


        // After confirmation, the application flow usually returns to a main screen.
        // The navigation logic (e.g., reopening MovieSelectionFrame) is handled
        // in the calling method (PaymentFrame.processBooking) after this dialog is shown.
    }
}