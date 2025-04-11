package com.ticketbook.movieapp.ui; // ui subpackage

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;

// Import necessary classes for database interaction
import com.mongodb.client.MongoCollection; // Import from MongoDB Driver (via Maven)
import com.mongodb.client.MongoDatabase; // Import from MongoDB Driver (via Maven)
import com.mongodb.client.result.InsertOneResult; // Import from MongoDB Driver (via Maven)
import com.mongodb.MongoException; // Import from MongoDB Driver (via Maven)
import org.bson.Document; // Import from MongoDB Driver (via Maven)
import org.bson.types.ObjectId; // Import from MongoDB Driver (via Maven)

import com.ticketbook.movieapp.db.DatabaseConnection; // Import from subpackage
import com.ticketbook.movieapp.models.Movie;        // Import from subpackage
import com.ticketbook.movieapp.models.User;         // Import from subpackage
import com.ticketbook.movieapp.models.Booking;      // Import from subpackage


/**
 * Simplified payment frame. In a real app, integrate with a payment API.
 * This example just confirms the booking directly.
 * Includes a Back button to return to Seat Selection.
 */
public class PaymentFrame extends JFrame implements ActionListener {

    private static final double TICKET_PRICE = 150.00; // Example price per ticket in INR

    private JLabel paymentInfoLabel;
    private JButton confirmPaymentButton;
    private JButton cancelButton;
    private JButton backButton; // <-- Added Back Button

    private User currentUser;
    private Movie selectedMovie;
    private String selectedShowtime;
    private List<String> selectedSeats;
    private double totalAmount;

    public PaymentFrame(User user, Movie movie, String showtime, List<String> seats) {
        if (user == null || movie == null || showtime == null || seats == null || seats.isEmpty()) {
             JOptionPane.showMessageDialog(null, "Error initializing payment. Missing data.", "Initialization Error", JOptionPane.ERROR_MESSAGE);
             throw new IllegalArgumentException("User, Movie, Showtime, and Seats cannot be null or empty.");
        }
        this.currentUser = user;
        this.selectedMovie = movie;
        this.selectedShowtime = showtime;
        this.selectedSeats = new ArrayList<>(seats); // Defensive copy
        this.totalAmount = this.selectedSeats.size() * TICKET_PRICE;

        setTitle("Payment Confirmation");
        setSize(450, 350); // Increased height slightly for better layout
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose on close to allow going back
        setLocationRelativeTo(null); // Center the window
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Payment Details ---
        paymentInfoLabel = new JLabel("", SwingConstants.CENTER);
        updatePaymentInfo(); // Set initial text
        paymentInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(new JScrollPane(paymentInfoLabel), BorderLayout.CENTER); // Make scrollable

        // --- Buttons ---
        confirmPaymentButton = new JButton("Confirm Booking & Payment");
        confirmPaymentButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        confirmPaymentButton.addActionListener(this);

        // --- Back Button --- <-- Added
        backButton = new JButton("Back");
        backButton.addActionListener(this);

        // --- Cancel Button ---
        cancelButton = new JButton("Cancel Booking"); // Renamed for clarity vs Back
        cancelButton.addActionListener(this);


        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.add(backButton); // <-- Added Back button
        buttonPanel.add(confirmPaymentButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void updatePaymentInfo() {
         String movieTitle = (selectedMovie != null && selectedMovie.getTitle() != null) ? selectedMovie.getTitle() : "N/A";
         String theatre = (selectedMovie != null && selectedMovie.getTheatre() != null) ? selectedMovie.getTheatre() : "N/A";
         String showtime = (selectedShowtime != null) ? selectedShowtime : "N/A";
         String seatsString = String.join(", ", selectedSeats);
         int numTickets = selectedSeats.size();

         String info = String.format(
            "<html><div style='text-align: center; padding: 10px;'>" +
            "<h2>Booking Summary</h2>" +
            "<b>Movie:</b> %s<br>" +
            "<b>Theatre:</b> %s<br>" +
            "<b>Showtime:</b> %s<br>" +
            "<b>Seats:</b> %s (%d tickets)<br><br>" +
            "<b style='font-size: 1.1em;'>Total Amount: ₹%.2f</b><br><br>" + // Use Rupee symbol
            "Click 'Confirm' to finalize your booking." +
            "</div></html>",
            movieTitle,
            theatre,
            showtime,
            seatsString,
            numTickets,
            totalAmount
        );
        paymentInfoLabel.setText(info);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == confirmPaymentButton) {
            processBooking();
        } else if (e.getSource() == backButton) { // <-- Handle Back Button Click
            handleBackButton();
        }
         else if (e.getSource() == cancelButton) {
            handleCancelButton();
        }
    }

    // --- Action Handler for Back Button --- <-- Added
    private void handleBackButton() {
        System.out.println("Back button clicked on Payment Frame. Returning to Seat Selection.");
        this.dispose(); // Close the current PaymentFrame

        // Re-create and show the SeatBookingFrame, passing the necessary data back
        // Ensure currentUser, selectedMovie, and selectedShowtime are not null
        if (currentUser != null && selectedMovie != null && selectedShowtime != null) {
             SwingUtilities.invokeLater(() -> {
                SeatBookingFrame seatFrame = new SeatBookingFrame(currentUser, selectedMovie, selectedShowtime);
                seatFrame.setVisible(true);
             });
        } else {
             // Fallback if data is somehow lost (should not happen with proper flow)
             JOptionPane.showMessageDialog(this, "Error returning to seat selection. Data missing.", "Navigation Error", JOptionPane.ERROR_MESSAGE);
             // Go back to movie selection or login as a fallback
             SwingUtilities.invokeLater(() -> new MovieSelectionFrame(currentUser).setVisible(true)); // Example fallback
        }
    }

     // --- Action Handler for Cancel Button ---
    private void handleCancelButton() {
         int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this booking entirely?", // Clarified message
                "Cancel Booking",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                 System.out.println("Booking cancelled by user on Payment Frame.");
                 this.dispose();
                 // Navigate back to movie selection after full cancellation
                 if (currentUser != null) {
                     SwingUtilities.invokeLater(() -> new MovieSelectionFrame(currentUser).setVisible(true));
                 } else {
                     SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true)); // Fallback
                 }
            }
    }


    /**
     * Simulates payment success and saves the booking to the database.
     */
    private void processBooking() {
        System.out.println("Simulating successful payment...");

         if (currentUser == null || currentUser.getId() == null || selectedMovie == null || selectedMovie.getId() == null) {
              JOptionPane.showMessageDialog(this, "Cannot save booking: User or Movie information is missing.", "Booking Error", JOptionPane.ERROR_MESSAGE);
              return;
         }

        try {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> bookingsCollection = database.getCollection("bookings");

            // Create a new booking document manually
            Document bookingDoc = new Document("_id", new ObjectId()) // Generate a new booking ID
                .append("userId", currentUser.getId()) // User's ObjectId
                .append("movieId", selectedMovie.getId()) // Movie's ObjectId
                .append("movieTitle", selectedMovie.getTitle())
                .append("theatre", selectedMovie.getTheatre())
                .append("showtime", selectedShowtime)
                .append("seats", selectedSeats) // The list of seat strings
                .append("totalAmount", totalAmount)
                .append("bookingTime", new Date()); // Current time


            InsertOneResult result = bookingsCollection.insertOne(bookingDoc);
            ObjectId newBookingId = null;
            if (result.getInsertedId() != null) {
                 newBookingId = result.getInsertedId().asObjectId().getValue();
                 System.out.println("Booking successful! Saved to DB with ID: " + newBookingId);
            } else {
                 System.out.println("Booking saved, but could not retrieve confirmation ID.");
            }

            // --- Show Confirmation ---
            this.dispose(); // Close payment window
            ConfirmationDialog.showConfirmation(null, // Parent component (can be null)
                                                selectedMovie.getTitle(),
                                                selectedShowtime,
                                                selectedSeats,
                                                totalAmount,
                                                (newBookingId != null) ? newBookingId.toString() : "N/A");

             // After successful booking, navigate back to movie selection
             if (currentUser != null) {
                 SwingUtilities.invokeLater(() -> new MovieSelectionFrame(currentUser).setVisible(true));
             } else {
                 SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
             }


        } catch (MongoException me) {
             JOptionPane.showMessageDialog(this, "Database error saving booking: " + me.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
             me.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error processing booking: " + ex.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
