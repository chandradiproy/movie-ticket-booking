package com.ticketbook.movieapp.ui; // ui subpackage

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections; // For sorting seats display

// Import necessary classes for database interaction
import com.mongodb.client.MongoCollection; // Import from MongoDB Driver (via Maven)
import com.mongodb.client.MongoDatabase; // Import from MongoDB Driver (via Maven)
import com.mongodb.client.model.Filters; // Import from MongoDB Driver (via Maven)
import com.mongodb.client.FindIterable; // Import from MongoDB Driver (via Maven)
import com.mongodb.MongoException; // Import from MongoDB Driver (via Maven)
import org.bson.Document; // Import from MongoDB Driver (via Maven)
import org.bson.types.ObjectId; // Import from MongoDB Driver (via Maven)

import com.ticketbook.movieapp.db.DatabaseConnection; // Import from subpackage
import com.ticketbook.movieapp.models.Movie;        // Import from subpackage
import com.ticketbook.movieapp.models.User;         // Import from subpackage

/**
 * Frame for selecting seats for a specific movie and showtime.
 * Includes a Back button to return to Movie Selection.
 */
public class SeatBookingFrame extends JFrame implements ActionListener {

    private static final int NUM_ROWS = 5; // Example: 5 rows
    private static final int SEATS_PER_ROW = 8; // Example: 8 seats per row
    private static final Color AVAILABLE_COLOR = Color.GREEN.darker(); // Darker green
    private static final Color SELECTED_COLOR = Color.ORANGE; // Orange for selected
    private static final Color BOOKED_COLOR = Color.RED.darker(); // Darker red
    private static final Color SEAT_BORDER_COLOR = Color.DARK_GRAY;

    private JPanel seatPanel;
    private JButton proceedToPaymentButton;
    private JButton backButton; // <-- Added Back Button
    private JLabel infoLabel; // Shows selected movie/time/seats
    private List<JToggleButton> seatButtons;
    private Set<String> selectedSeats; // Store selected seat IDs (e.g., "A1", "B5")
    private Set<String> bookedSeats; // Store already booked seat IDs

    private User currentUser;
    private Movie selectedMovie;
    private String selectedShowtime;

    public SeatBookingFrame(User user, Movie movie, String showtime) {
        if (user == null || movie == null || showtime == null || movie.getId() == null) {
             JOptionPane.showMessageDialog(null, "Error initializing seat selection. Missing data.", "Initialization Error", JOptionPane.ERROR_MESSAGE);
             throw new IllegalArgumentException("User, Movie, Showtime, and Movie ID cannot be null.");
        }

        this.currentUser = user;
        this.selectedMovie = movie;
        this.selectedShowtime = showtime;
        this.selectedSeats = new HashSet<>();
        this.bookedSeats = new HashSet<>();
        this.seatButtons = new ArrayList<>();


        setTitle("Select Seats for " + movie.getTitle() + " (" + showtime + ")");
        setSize(700, 500); // Adjusted size
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose on close to allow going back
        setLocationRelativeTo(null); // Center the window

        loadBookedSeats(); // Load booked seats before creating UI
        initComponents();
        updateInfoLabel();
    }

    /**
     * Loads the currently booked seats for this specific movie and showtime from the database.
     */
    private void loadBookedSeats() {
        bookedSeats.clear();
        if (selectedMovie == null || selectedMovie.getId() == null || selectedShowtime == null) {
            System.err.println("Cannot load booked seats: Movie, Movie ID, or Showtime is null.");
            return;
        }

        try {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> bookingsCollection = database.getCollection("bookings");

            FindIterable<Document> results = bookingsCollection.find(
                Filters.and(
                    Filters.eq("movieId", selectedMovie.getId()), // Compare with ObjectId
                    Filters.eq("showtime", selectedShowtime)
                )
            );

            for (Document booking : results) {
                Object seatsObject = booking.get("seats");
                 if (seatsObject instanceof List) {
                     try {
                         @SuppressWarnings("unchecked")
                         List<String> seatsInBooking = (List<String>) seatsObject;
                         if (seatsInBooking != null) {
                             bookedSeats.addAll(seatsInBooking);
                         }
                     } catch (ClassCastException cce) {
                          System.err.println("Warning: Seats list in booking " + booking.getObjectId("_id") + " is not List<String>. Skipping seats.");
                     }
                 }
            }
            System.out.println("Loaded booked seats for " + selectedMovie.getTitle() + " (" + selectedShowtime + "): " + bookedSeats);

        } catch (MongoException me) {
             JOptionPane.showMessageDialog(this, "Database error loading booked seats: " + me.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
             me.printStackTrace();
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading booked seats: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Info Panel (Top) ---
        infoLabel = new JLabel("Select your seats.", SwingConstants.CENTER);
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(infoLabel, BorderLayout.NORTH);

        // --- Seat Panel (Center) ---
        seatPanel = new JPanel(new GridLayout(NUM_ROWS, SEATS_PER_ROW, 8, 8));
        seatPanel.setBorder(BorderFactory.createTitledBorder("Seats (A=Front)"));
        createSeatButtons();
        add(new JScrollPane(seatPanel), BorderLayout.CENTER);

        // --- Legend Panel ---
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        legendPanel.add(createLegendLabel("Available", AVAILABLE_COLOR));
        legendPanel.add(createLegendLabel("Selected", SELECTED_COLOR));
        legendPanel.add(createLegendLabel("Booked", BOOKED_COLOR));


        // --- Control Panel (Bottom) ---
        JPanel controlPanel = new JPanel(new BorderLayout(10, 5)); // Use BorderLayout

        // --- Bottom Button Panel (using FlowLayout for Back and Proceed) --- <-- Contains Back Button
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        backButton = new JButton("Back to Movies"); // <-- Back Button Instantiation
        backButton.addActionListener(this); // <-- Add listener

        proceedToPaymentButton = new JButton("Proceed to Payment");
        proceedToPaymentButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        proceedToPaymentButton.addActionListener(this);
        proceedToPaymentButton.setEnabled(false);

        bottomButtonPanel.add(backButton); // <-- Add Back button to panel
        bottomButtonPanel.add(proceedToPaymentButton);


        controlPanel.add(legendPanel, BorderLayout.NORTH); // Legend at the top of control panel
        controlPanel.add(bottomButtonPanel, BorderLayout.SOUTH); // Button panel at the bottom

        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Helper method to create a label for the legend.
     */
    private JLabel createLegendLabel(String text, Color color) {
        JLabel label = new JLabel(text);
         ImageIcon icon = new ImageIcon(new java.awt.image.BufferedImage(12, 12, java.awt.image.BufferedImage.TYPE_INT_RGB) {
             {
                 Graphics g = createGraphics();
                 g.setColor(color);
                 g.fillRect(0, 0, 12, 12);
                 g.setColor(SEAT_BORDER_COLOR);
                 g.drawRect(0, 0, 11, 11);
                 g.dispose();
             }
         });
         label.setIcon(icon);
         label.setIconTextGap(5);
         label.setHorizontalTextPosition(JLabel.RIGHT);
        return label;
    }


    /**
     * Creates the grid of seat buttons.
     */
    private void createSeatButtons() {
        seatButtons.clear();
        seatPanel.removeAll();

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int seatNum = 0; seatNum < SEATS_PER_ROW; seatNum++) {
                char rowChar = (char) ('A' + row);
                String seatId = String.format("%c%d", rowChar, seatNum + 1);

                JToggleButton seatButton = new JToggleButton(seatId);
                seatButton.setMargin(new Insets(5, 5, 5, 5));
                seatButton.setFont(new Font("Monospaced", Font.BOLD, 12));
                seatButton.setFocusPainted(false);
                seatButton.setForeground(Color.WHITE);

                if (bookedSeats.contains(seatId)) {
                    seatButton.setEnabled(false);
                    seatButton.setBackground(BOOKED_COLOR);
                    seatButton.setToolTipText("Seat already booked");
                    seatButton.setSelected(true);
                } else {
                    seatButton.setEnabled(true);
                    seatButton.setBackground(AVAILABLE_COLOR);
                    seatButton.setToolTipText("Seat Available - Click to Select");
                    seatButton.addActionListener(e -> handleSeatToggle(seatButton, seatId));
                }

                seatButtons.add(seatButton);
                seatPanel.add(seatButton);
            }
        }
        seatPanel.revalidate();
        seatPanel.repaint();
    }

    /**
     * Handles toggling a seat button (selecting/deselecting).
     */
    private void handleSeatToggle(JToggleButton button, String seatId) {
        if (button.isSelected()) {
            selectedSeats.add(seatId);
            button.setBackground(SELECTED_COLOR);
            button.setToolTipText("Seat Selected - Click to Deselect");
        } else {
            selectedSeats.remove(seatId);
            button.setBackground(AVAILABLE_COLOR);
             button.setToolTipText("Seat Available - Click to Select");
        }
        updateInfoLabel();
        proceedToPaymentButton.setEnabled(!selectedSeats.isEmpty());
    }

    /**
     * Updates the information label at the top.
     */
    private void updateInfoLabel() {
         String movieTitle = (selectedMovie != null) ? selectedMovie.getTitle() : "N/A";
         String showtime = (selectedShowtime != null) ? selectedShowtime : "N/A";

         String infoText = String.format("Movie: %s | Showtime: %s | Selected Seats: %d",
                                         movieTitle,
                                         showtime,
                                         selectedSeats.size());
        if (!selectedSeats.isEmpty()) {
             List<String> sortedSeats = new ArrayList<>(selectedSeats);
             Collections.sort(sortedSeats);
             infoText += " (" + String.join(", ", sortedSeats) + ")";
        }
        infoLabel.setText(infoText);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == proceedToPaymentButton) {
            handleProceedToPayment();
        } else if (e.getSource() == backButton) { // <-- Handle Back Button Click
            handleBackButton();
        }
    }

    // --- Action Handler for Back Button --- <-- Added
    private void handleBackButton() {
        System.out.println("Back button clicked on Seat Frame. Returning to Movie Selection.");
        this.dispose(); // Close the current SeatBookingFrame

        // Re-create and show the MovieSelectionFrame, passing the user back
        if (currentUser != null) {
             SwingUtilities.invokeLater(() -> {
                 MovieSelectionFrame movieFrame = new MovieSelectionFrame(currentUser);
                 movieFrame.setVisible(true);
             });
        } else {
             // Fallback if user data is lost (should not happen)
             JOptionPane.showMessageDialog(this, "Error returning to movie selection. User data missing.", "Navigation Error", JOptionPane.ERROR_MESSAGE);
             SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true)); // Go back to login
        }
    }

    // --- Action Handler for Proceed Button ---
    private void handleProceedToPayment() {
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one seat.", "No Seats Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("Proceeding to payment for seats: " + selectedSeats);
        List<String> seatsToBook = new ArrayList<>(selectedSeats); // Pass a copy

        this.dispose();

         if (currentUser == null || selectedMovie == null || selectedShowtime == null) {
              JOptionPane.showMessageDialog(null, "Error proceeding to payment. Missing data.", "Error", JOptionPane.ERROR_MESSAGE);
              // Consider navigating back safely
              handleBackButton(); // Use back button logic as a fallback
              return;
         }
        // Use invokeLater for safety when creating new frames from event handlers
        SwingUtilities.invokeLater(() -> {
             PaymentFrame paymentFrame = new PaymentFrame(currentUser, selectedMovie, selectedShowtime, seatsToBook);
             paymentFrame.setVisible(true);
        });
    }
}
