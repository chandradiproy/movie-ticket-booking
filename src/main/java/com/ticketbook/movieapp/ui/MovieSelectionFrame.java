package com.ticketbook.movieapp.ui; // Updated package

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

// Import necessary classes for database interaction
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.MongoException; // Import MongoException
import org.bson.Document;
import org.bson.types.ObjectId; // Import ObjectId

import com.ticketbook.movieapp.db.DatabaseConnection; // Updated import
import com.ticketbook.movieapp.models.Movie;        // Updated import
import com.ticketbook.movieapp.models.User;         // Updated import

/**
 * Frame for selecting a movie and showtime.
 */
public class MovieSelectionFrame extends JFrame implements ActionListener {

    private JList<Movie> movieList; // JList to display movies
    private DefaultListModel<Movie> movieListModel;
    private JComboBox<String> showtimeComboBox; // Dropdown for showtimes
    private JButton selectSeatsButton;
    private JTextArea movieDetailsArea; // To show description, etc.
    private User currentUser; // Store the logged-in user

    // Store movies loaded from DB
    private List<Movie> availableMovies;

    public MovieSelectionFrame(User user) {
        this.currentUser = user; // Receive the logged-in user

        // Use null check for username if User object might be null initially
        String welcomeMessage = "Select Movie";
        if (user != null && user.getUsername() != null) {
            welcomeMessage += " - Welcome, " + user.getUsername();
        }
        setTitle(welcomeMessage);
        setSize(600, 450); // Adjusted size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Or DISPOSE_ON_CLOSE if login should persist
        setLocationRelativeTo(null); // Center the window

        availableMovies = new ArrayList<>();
        initComponents();
        loadMoviesFromDatabase(); // Load movies when the frame is created
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)); // Main layout with spacing
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // --- Movie List Panel (Left) ---
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Available Movies"));

        movieListModel = new DefaultListModel<>();
        movieList = new JList<>(movieListModel);
        movieList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Add a listener to update details when a movie is selected
        movieList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Prevent double events
                updateMovieDetails();
            }
        });

        JScrollPane listScrollPane = new JScrollPane(movieList);
        listPanel.add(listScrollPane, BorderLayout.CENTER);

        // --- Details and Selection Panel (Right) ---
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS)); // Vertical layout
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Details & Showtime"));

        // Movie Details Area
        movieDetailsArea = new JTextArea(8, 30); // Rows, Columns
        movieDetailsArea.setEditable(false);
        movieDetailsArea.setLineWrap(true);
        movieDetailsArea.setWrapStyleWord(true);
        movieDetailsArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane detailsScrollPane = new JScrollPane(movieDetailsArea);
        detailsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT); // Align components

        // Showtime Label and ComboBox
        JLabel showtimeLabel = new JLabel("Select Showtime:");
        showtimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        showtimeComboBox = new JComboBox<>(); // Populate this based on selected movie
        showtimeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showtimeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, showtimeComboBox.getPreferredSize().height)); // Prevent stretching vertically

        // Select Seats Button
        selectSeatsButton = new JButton("Select Seats");
        selectSeatsButton.addActionListener(this);
        selectSeatsButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectSeatsButton.setEnabled(false); // Initially disabled

        // Add components to details panel
        detailsPanel.add(new JLabel("Movie Details:")); // Changed label
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        detailsPanel.add(detailsScrollPane);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        detailsPanel.add(showtimeLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        detailsPanel.add(showtimeComboBox);
        detailsPanel.add(Box.createVerticalGlue()); // Pushes button to bottom
        detailsPanel.add(selectSeatsButton);

        // Add panels to main panel
        mainPanel.add(listPanel, BorderLayout.WEST);
        mainPanel.add(detailsPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    /**
     * Loads movie data from the MongoDB 'movies' collection.
     */
    private void loadMoviesFromDatabase() {
        movieListModel.clear(); // Clear existing list
        availableMovies.clear(); // Clear internal list

        try {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> moviesCollection = database.getCollection("movies"); // Assuming collection name is 'movies'

            FindIterable<Document> movieDocs = moviesCollection.find();

            for (Document doc : movieDocs) {
                // Create a Movie object from the Document using the Movie model's setters or constructor
                Movie movie = new Movie();
                movie.setId(doc.getObjectId("_id")); // Get ObjectId
                movie.setTitle(doc.getString("title"));
                movie.setDescription(doc.getString("description"));
                movie.setGenre(doc.getString("genre")); // Assuming genre field exists
                // Handle potential null or incorrect type for integer field
                Object durationObj = doc.get("durationMinutes");
                if (durationObj instanceof Integer) {
                    movie.setDurationMinutes((Integer) durationObj);
                } else if (durationObj instanceof Number) { // Handle Double, Long etc.
                     movie.setDurationMinutes(((Number) durationObj).intValue());
                } // else leave as default 0 or handle error

                movie.setRating(doc.getString("rating")); // Assuming rating field exists
                movie.setTheatre(doc.getString("theatre"));

                // Handle showtimes list (assuming it's stored as List<String>)
                Object showtimesObj = doc.get("showtimes");
                if (showtimesObj instanceof List) {
                    // Check list content type if necessary, assuming List<String> here
                    try {
                         @SuppressWarnings("unchecked") // Suppress warning for cast
                         List<String> showtimesList = (List<String>) showtimesObj;
                         movie.setShowtimes(showtimesList);
                    } catch (ClassCastException cce) {
                         System.err.println("Warning: Showtime list for movie " + movie.getTitle() + " is not List<String>. Skipping showtimes.");
                         movie.setShowtimes(new ArrayList<>()); // Set empty list
                    }
                } else {
                    movie.setShowtimes(new ArrayList<>()); // Set empty list if field is missing or wrong type
                }


                availableMovies.add(movie);
                movieListModel.addElement(movie); // Add movie to the JList model (uses movie.toString())
            }

            if (!movieListModel.isEmpty()) {
                movieList.setSelectedIndex(0); // Select the first movie by default
            } else {
                 movieDetailsArea.setText("No movies currently available.");
                 selectSeatsButton.setEnabled(false);
                 showtimeComboBox.setEnabled(false);
            }

        } catch (MongoException me) {
             JOptionPane.showMessageDialog(this, "Database error loading movies: " + me.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
             me.printStackTrace(); // Log error
        }
         catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing movie data: " + e.getMessage(), "Data Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Log error
        }
    }

    /**
     * Updates the details area and showtime combo box based on the selected movie.
     */
    private void updateMovieDetails() {
        Movie selectedMovie = movieList.getSelectedValue();

        if (selectedMovie != null) {
            // Build details string using available fields from Movie object
            StringBuilder details = new StringBuilder();
            details.append("Title: ").append(selectedMovie.getTitle()).append("\n");
            if (selectedMovie.getGenre() != null) {
                 details.append("Genre: ").append(selectedMovie.getGenre()).append("\n");
            }
             if (selectedMovie.getDurationMinutes() > 0) {
                 details.append("Duration: ").append(selectedMovie.getDurationMinutes()).append(" mins\n");
            }
             if (selectedMovie.getRating() != null) {
                 details.append("Rating: ").append(selectedMovie.getRating()).append("\n");
            }
            if (selectedMovie.getTheatre() != null) {
                 details.append("Theatre: ").append(selectedMovie.getTheatre()).append("\n");
            }
            details.append("\nDescription:\n").append(selectedMovie.getDescription());

            movieDetailsArea.setText(details.toString());
            movieDetailsArea.setCaretPosition(0); // Scroll to top

            // Populate showtimes
            showtimeComboBox.removeAllItems(); // Clear previous items
            List<String> showtimes = selectedMovie.getShowtimes(); // Use getter
            if (showtimes != null && !showtimes.isEmpty()) {
                for (String showtime : showtimes) {
                    showtimeComboBox.addItem(showtime);
                }
                showtimeComboBox.setEnabled(true);
                selectSeatsButton.setEnabled(true); // Enable button only if showtimes exist
            } else {
                showtimeComboBox.addItem("No showtimes available");
                showtimeComboBox.setEnabled(false);
                selectSeatsButton.setEnabled(false);
            }
        } else {
            movieDetailsArea.setText("");
            showtimeComboBox.removeAllItems();
            showtimeComboBox.setEnabled(false);
            selectSeatsButton.setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectSeatsButton) {
            handleSeatSelection();
        }
    }

    private void handleSeatSelection() {
        Movie selectedMovie = movieList.getSelectedValue();
        String selectedShowtime = (String) showtimeComboBox.getSelectedItem();

        if (selectedMovie == null) {
            JOptionPane.showMessageDialog(this, "Please select a movie.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedShowtime == null || selectedShowtime.isEmpty() || selectedShowtime.equals("No showtimes available")) {
             JOptionPane.showMessageDialog(this, "Please select a valid showtime.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Proceed to the seat booking frame
        System.out.println("Selected Movie: " + selectedMovie.getTitle() + ", Showtime: " + selectedShowtime);

        // Close this frame and open the seat booking frame
        this.dispose();
        // Ensure currentUser is not null before passing
        if (currentUser == null) {
             // Handle case where user somehow became null (e.g., show login again)
             JOptionPane.showMessageDialog(this, "User session error. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
             new LoginFrame().setVisible(true); // Example: Show login
             return;
        }
        SeatBookingFrame seatFrame = new SeatBookingFrame(currentUser, selectedMovie, selectedShowtime);
        seatFrame.setVisible(true);
    }
}