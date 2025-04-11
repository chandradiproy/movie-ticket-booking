package com.ticketbook.movieapp.ui; // Updated package

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Import necessary classes for database interaction
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq; // For easier query building

import com.ticketbook.movieapp.db.DatabaseConnection; // Updated import
import com.ticketbook.movieapp.models.User; // Updated import

/**
 * The login window for the Movie Ticket Booking System.
 */
public class LoginFrame extends JFrame implements ActionListener {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton; // Optional: Add registration functionality later
    private User loggedInUser; // To store user details after successful login

    public LoginFrame() {
        setTitle("Movie Ticket Booking - Login");
        setSize(400, 250); // Adjusted size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Username ---
        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END; // Align label to the right
        panel.add(userLabel, gbc);

        usernameField = new JTextField(20); // Increased width
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START; // Align field to the left
        panel.add(usernameField, gbc);

        // --- Password ---
        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(passLabel, gbc);

        passwordField = new JPasswordField(20); // Increased width
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(passwordField, gbc);

        // --- Buttons ---
        loginButton = new JButton("Login");
        loginButton.addActionListener(this); // Register action listener

        registerButton = new JButton("Register"); // Placeholder for registration
        registerButton.addActionListener(this); // Add action listener if implementing
        registerButton.setEnabled(false); // Disable for now

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Center buttons
        buttonPanel.add(loginButton);
        // buttonPanel.add(registerButton); // Uncomment when registration is added

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Span across both columns
        gbc.fill = GridBagConstraints.NONE; // Don't stretch buttons horizontally
        gbc.anchor = GridBagConstraints.CENTER; // Center the button panel
        gbc.insets = new Insets(15, 5, 5, 5); // Add top margin before buttons
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            handleLogin();
        } else if (e.getSource() == registerButton) {
            // Handle registration (Open a new registration frame/dialog)
            JOptionPane.showMessageDialog(this, "Registration functionality not yet implemented.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()); // Get password safely

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            MongoDatabase database = DatabaseConnection.getDatabase();
            // Assuming you have a 'users' collection
            MongoCollection<Document> usersCollection = database.getCollection("users");

            // Find the user document matching the username
            // IMPORTANT: Store passwords securely (hashed)! This is a basic example.
            Document userDoc = usersCollection.find(eq("username", username)).first();

            if (userDoc != null) {
                // **VERY IMPORTANT SECURITY NOTE:**
                // In a real application, NEVER store plain text passwords.
                // You should store a HASH of the password (e.g., using bcrypt).
                // During login, hash the entered password and compare it with the stored hash.
                String storedPassword = userDoc.getString("password"); // Retrieve plain password (INSECURE)

                // TODO: Replace this plain text comparison with secure hash comparison
                if (password.equals(storedPassword)) { // Compare plain passwords (INSECURE)
                    // Login successful
                    JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Store user details (using the User model)
                    // Assuming the User model has appropriate constructor/setters
                    loggedInUser = new User(userDoc.getString("username"));
                    // If storing ObjectId in User model:
                     loggedInUser.setId(userDoc.getObjectId("_id")); // Store the actual ID

                    // Close login window and open movie selection window
                    this.dispose(); // Close the current login frame
                    MovieSelectionFrame movieFrame = new MovieSelectionFrame(loggedInUser);
                    movieFrame.setVisible(true);

                } else {
                    // Incorrect password
                    JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // User not found
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error during login: " + ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Log the error for debugging
        }
    }
}