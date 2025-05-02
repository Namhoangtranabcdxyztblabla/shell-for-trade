import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;
import java.awt.*;
import javax.swing.*;


/**
 *
 * This is the client class of the Project
 *
 * @author Anchit, Nam, Terry, Garv
 * @version April 20 2025
 *
 */

public class Client{
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private User currentUser;
    private boolean connected;

    /**
     * Constructs a new Client instance.
     * The client starts disconnected with no current user.
     */
    public Client() {
        this.connected = false;
        this.currentUser = null;
    }

    /**
     * Attempts to connect to the server at the specified host and port.
     *
     * @param host The server hostname or IP address
     * @param port The server port number
     * @return true if connection was successful, false otherwise
     */
    public boolean connect(String host, int port) {
        if (connected) {
            System.out.println("Already connected to a server");
            return false;
        }

        try {
            socket = new Socket(host, port);
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());
            connected = true;
            System.out.println("Connect successfully");
            return true;
        } catch (IOException e) {
            System.out.println("Fail to connect to server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Disconnects from the server and closes all resources.
     *
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        try {
            connected = false;
            currentUser = null;

            // Close streams and socket
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Disconnected from server");
        } catch (IOException e) {
            System.out.println("Error disconnecting from server: " + e.getMessage());
        }
    }

    /**
     * Checks if the client is currently connected to the server.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }
    /**
     * Reads an object from the server.
     *
     * @return The object read from the server
     * @throws IOException If there is an error reading from the connection
     * @throws ClassNotFoundException If the class of the serialized object cannot be found
     */
    public Object read() throws IOException, ClassNotFoundException {
        if (!connected) {
            throw new IOException("Not connected to server");
        }
        return input.readObject();
    }

    /**
     * Writes an object to the server.
     *
     * @param command The object to write to the server
     * @throws IOException If there is an error writing to the connection
     */
    public void write(Object command) throws IOException {
        output.writeObject(command);
        output.flush();
    }

    public static void main(String[] args) {
        Client client = new Client();
        boolean connected = client.connect("localhost", 4242);

        if (!connected) {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to server. Please make sure the server is running.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Create and show the GUI
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI(client);
            gui.show();
        });
    }

    /**
     * Attempts to purchase an item from a seller.
     *
     * @param sellerName The username of the seller
     * @param itemName The name of the item to buy
     * @param price The price of the item
     * @return true if the purchase was successful, false otherwise
     */
    private boolean buyItem(String sellerName, String itemName, double price) {
        try {
            write("buyItem");
            write(sellerName);
            write(itemName);
            write(price);

            String response = (String) read();
            if (response.startsWith("Success")) {
                System.out.println(response.substring(9)); // Remove "SUCCESS: " prefix
                return true;
            } else {
                System.out.println(response);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error buying item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds money to the user's balance.
     *
     * @param money The amount to add to the balance
     * @return true if the operation was successful, false otherwise
     */
    private boolean addBalance(double money) {
        if (money <= 0) {
            System.out.println("Amount must be positive");
            return false;
        }

        try {
            write("addBalance");
            write(money);

            String response = (String) read();
            if (response.startsWith("Success")) {
                // print the message  (the message server sends after the word "Success")
                System.out.println(response.substring(9));

                // Update the local user object
                if (currentUser != null) {
                    currentUser.addBalance(money);
                }
                return true;
            } else {
                System.out.println(response);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error adding balance: " + e.getMessage());
            return false;
        }
    }

    /**
     * Withdraws money from the user's balance.
     *
     * @param amount The amount to withdraw
     * @return true if the withdrawal was successful, false otherwise
     */
    public boolean withdrawBalance(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive");
            return false;
        }

        try {
            write("withdrawBalance");
            write(amount);

            String response = (String) read();
            if (response.startsWith("Success")) {
                System.out.println(response.substring(9)); // Remove "Success: " prefix in the message to the user

                // Update the local user object
                if (currentUser != null) {
                    currentUser.setBalance(currentUser.getBalance() - amount);
                }
                return true;
            } else {
                System.out.println(response);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error withdrawing balance: " + e.getMessage());
            return false;
        }
    }

    /**
     * Changes the email address of the current user.
     *
     * @param newEmail The new email address
     */
    public void changeEmail(String newEmail) {
        try {
            write("changeEmail");
            write(newEmail);

            String response = (String) read();
            if (response.equals("Success")) {
                currentUser.setEmail(newEmail);
                System.out.println("Email updated successfully");
            } else {
                System.out.println(response);
            }
        } catch (Exception e) {
            System.out.println("Error updating email: " + e.getMessage());
        }
    }

    /**
     * Changes the username of the current user.
     *
     * @param newUsername The new username
     */
    public void changeUsername(String newUsername) {
        try {
            write("changeUsername");
            write(newUsername);

            String response = (String) read();
            if (response.equals("Success")) {
                currentUser.setName(newUsername);
                System.out.println("Username updated successfully");
            } else {
                System.out.println(response);
            }
        } catch (Exception e) {
            System.out.println("Error updating username: " + e.getMessage());
        }
    }

    /**
     * Changes the password of the current user.
     *
     * @param newPassword The new password
     */
    public void changePassword(String newPassword) {
        try {
            write("changePassword");
            write(newPassword);

            String response = (String) read();
            if (response.startsWith("Success")) {
                System.out.println("Password updated successfully");
            } else {
                System.out.println(response);
            }
        } catch (Exception e) {
            System.out.println("Error updating password: " + e.getMessage());
        }
    }


    /**
     * Retrieves the message history for the current user.
     * The returned map contains message partners as keys and lists of messages as values.
     * using HashMap to distinguish each user who send message to currentUser
     * Using the message partner username as the key
     *
     * @return A map of message partners to lists of messages
     */
    public HashMap<String, ArrayList<String>> getMessageHistory() {
        try {
            write("getMessageHistory");

            // receive data from server (should be an arraylist of all messages)
            Object response = read();
            if (response instanceof HashMap<?,?>) {
                return (HashMap<String, ArrayList<String>>) response;
            } else {
                System.out.println("Error: Unexpected response from server");
                return new HashMap<>();
            }
        } catch (Exception e) {
            System.out.println("Error retrieving message history: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sends a message to another user.
     *
     * @param receiverUsername The username of the message recipient
     * @param message The content of the message to send
     */
    public void sendMessage(String receiverUsername, String message) {
        try {
            //send data to the server
            write("sendMessage");
            write(receiverUsername);
            write(message);

            //receive data back from the server
            String response = (String) read();
            if (response.equals("Success")) {
                System.out.println("Message sent successfully");
            } else {
                System.out.println(response);
            }
        } catch (Exception e) {
            System.out.println("Error sending message: " + e.getMessage());
        }


    }

    /**
     * Searches for items matching the given search term.
     *
     * @param searchTerm The term to search for in item names or descriptions
     * @return A list of matching items
     * @throws IOException If there is an error communicating with the server
     */
    public ArrayList<Item> searchItems(String searchTerm) throws IOException {
        try {
            write("search");
            write(searchTerm);

            Object response = read();
            if (response instanceof ArrayList) {
                return (ArrayList<Item>) response;
            } else {
                System.out.println("Error: Unexpected response from server");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("Error searching items: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all items available in the marketplace.
     *
     * @return A list of all items
     * @throws IOException If there is an error communicating with the server
     */
    public ArrayList<Item> viewItems() throws IOException {
        try {
            write("viewItems");

            Object response = read();
            if (response instanceof ArrayList<?> list) {
                // Cast with proper type checking
                if (!list.isEmpty() && list.getFirst() instanceof Item) {
                    return (ArrayList<Item>) list;
                } else {
                    System.out.println("Error: Received empty or non-Item list");
                    return new ArrayList<>();
                }
            } else {
                System.out.println("Error: Unexpected response from server: " + response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("Error retrieving items: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Posts a new item for sale in the marketplace.
     *
     * @param itemName The name of the item
     * @param price The price of the item
     * @param description A description of the item
     */
    public boolean postItem(String itemName, double price, String description) {
        try {
            write("postItem");
            write(itemName);
            write(price);
            write(description);

            String response = (String) read();
            if (response.equalsIgnoreCase("Success")) {
                System.out.println("Item posted successfully");
                return true;
            } else {
                System.out.println(response);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error posting item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Logs the current user out of the system.
     *
     * @return true if logout was successful, false otherwise
     */
    public boolean logout() {
        try {
            write("logout");

            String response = (String) read();
            if (response.equalsIgnoreCase("Success")) {
                currentUser = null;
                System.out.println("Logged out successfully");
                return true;
            } else {
                System.out.println(response);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error during logout: " + e.getMessage());
            return false;
        }
    }

    /**
     * Attempts to log in to the system with the given credentials.
     *
     * @param emailOrUsername The email or username of the user
     * @param password The password of the user
     * @return true if login was successful, false otherwise
     */
    public boolean login(String emailOrUsername, String password) {
        try {
            //send data to server
            write("login");
            write(emailOrUsername);
            write(password);

            //handle response
            String response = (String) read();
            if (response.equals("Success")) {
                Object userObj = read();
                //check if userObj is a user (it should be the currentUser that server sends us)
                if (userObj instanceof User) {
                    currentUser = (User) userObj;
                    System.out.println("Login successful! Welcome, " + currentUser.getName());
                    return true;
                } else {
                    System.out.println("Error: Failed to receive user data");
                    return false;
                }
            } else {
                System.out.println(response);
//                String correctPassword = (String) read(); //for testing
//                System.out.println(correctPassword);      //for testing
                return false;
            }
        } catch (Exception e) {
            System.out.println("Fail to login: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates a new user account.
     *
     * @param username The username for the new account
     * @param email The email address for the new account
     * @param newPassword The password for the new account
     * @param balance The initial balance for the new account
     * @return true if account creation was successful, false otherwise
     */
    public boolean createAccount(String username, String email, String newPassword, double balance) {
        try {
            write("createAccount");
            write(username);
            write(email);
            write(newPassword);
            write(balance);

            String response = (String) read();
            if (response.startsWith("Success")) {
                System.out.println("Account created successfully");
                return true;
            } else {
                System.out.println(response);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error creating account: " + e.getMessage());
        }

        return true;
    }

    /**
     * Deletes the current user's account.
     *
     * @return true if account deletion was successful, false otherwise
     */public boolean deleteAccount() {
        try {
            write("deleteAccount");

            String response = (String) read();
            if (response.startsWith("Success")) {
                currentUser = null;
                System.out.println("Account deleted successfully");
                return true;
            } else {
                System.out.println(response);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error deleting account: " + e.getMessage());
            return false;
        }
    }

    static class ClientGUI {
        private final Client client;
        private JFrame mainFrame;
        private JPanel currentPanel;
        private CardLayout cardLayout;
        private JPanel mainPanel;

        public ClientGUI(Client client) {
            this.client = client;
            initializeGUI();
        }

        private void initializeGUI() {
            mainFrame = new JFrame("BoilerTrade");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(800, 600);
            mainFrame.setLocationRelativeTo(null);

            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);

            // Create and add panels in order
            mainPanel.add(createLoginPanel(), "LOGIN");
            mainPanel.add(createSignupPanel(), "SIGNUP");

            // Add other panels
            mainPanel.add(createPostItemPanel(), "POST_ITEM");
            mainPanel.add(createSearchItemsPanel(), "SEARCH_ITEMS");
            mainPanel.add(createViewItemsPanel(), "VIEW_ITEMS");
            mainPanel.add(createSendMessagePanel(), "SEND_MESSAGE");
            mainPanel.add(createViewMessagesPanel(), "VIEW_MESSAGES");
            mainPanel.add(createProfilePanel(), "PROFILE");
            mainPanel.add(createBalancePanel(), "BALANCE");

            // Create main menu panel last
            mainPanel.add(createMainMenuPanel(), "MAIN_MENU");

            mainFrame.add(mainPanel);
            showLoginPanel();
        }

        private JPanel createLoginPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            JLabel titleLabel = new JLabel("Login to BoilerTrade");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(titleLabel, gbc);

            gbc.gridwidth = 1;
            gbc.gridy = 1;
            panel.add(new JLabel("Email/Username:"), gbc);
            gbc.gridx = 1;
            JTextField usernameField = new JTextField(20);
            panel.add(usernameField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            JTextField passwordField = new JTextField(20);
            panel.add(passwordField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(e -> {
                String username = usernameField.getText();
                String password = passwordField.getText();
                if (client.login(username, password)) {
                    showMainMenuPanel();
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Login failed. Please try again.");
                }
            });
            panel.add(loginButton, gbc);

            gbc.gridy = 4;
            JButton signupButton = new JButton("Create New Account");
            signupButton.addActionListener(e -> showSignupPanel());
            panel.add(signupButton, gbc);

            return panel;
        }

        private JPanel createSignupPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            JLabel titleLabel = new JLabel("Create New Account");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(titleLabel, gbc);

            gbc.gridwidth = 1;
            gbc.gridy = 1;
            panel.add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            JTextField usernameField = new JTextField(20);
            panel.add(usernameField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1;
            JTextField emailField = new JTextField(20);
            panel.add(emailField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            JTextField passwordField = new JTextField(20);
            panel.add(passwordField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 4;
            panel.add(new JLabel("Initial Balance:"), gbc);
            gbc.gridx = 1;
            JTextField balanceField = new JTextField(20);
            panel.add(balanceField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            JButton createButton = new JButton("Create Account");
            createButton.addActionListener(e -> {
                try {
                    String username = usernameField.getText().trim();
                    String email = emailField.getText().trim();
                    String password = passwordField.getText().trim();
                    String balanceText = balanceField.getText().trim();

                    if (username.isEmpty()) {
                        JOptionPane.showMessageDialog(mainFrame, "Please enter a username.");
                        return;
                    }
                    if (email.isEmpty()) {
                        JOptionPane.showMessageDialog(mainFrame, "Please enter an email.");
                        return;
                    }
                    if (password.isEmpty()) {
                        JOptionPane.showMessageDialog(mainFrame, "Please enter a password.");
                        return;
                    }
                    if (balanceText.isEmpty()) {
                        JOptionPane.showMessageDialog(mainFrame, "Please enter an initial balance.");
                        return;
                    }

                    double balance = Double.parseDouble(balanceText);
                    if (balance < 0) {
                        JOptionPane.showMessageDialog(mainFrame, "Balance cannot be negative.");
                        return;
                    }

                    if (client.createAccount(username, email, password, balance)) {
                        JOptionPane.showMessageDialog(mainFrame, "Account created successfully! You can now log in with your credentials.");
                        showLoginPanel();
                    } else {
                        // The error message is already printed by the client
                        JOptionPane.showMessageDialog(mainFrame, "Account creation failed. Please check the console for details.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Please enter a valid balance amount.");
                }
            });
            panel.add(createButton, gbc);

            gbc.gridy = 6;
            JButton backButton = new JButton("Back to Login");
            backButton.addActionListener(e -> showLoginPanel());
            panel.add(backButton, gbc);

            return panel;
        }

        private JPanel createMainMenuPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel welcomeLabel;
            if (client.currentUser != null) {
                welcomeLabel = new JLabel("Welcome, " + client.currentUser.getName() + "!");
            } else {
                welcomeLabel = new JLabel("Welcome to BoilerTrade!");
            }
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(welcomeLabel, gbc);

            if (client.currentUser != null) {
                gbc.gridwidth = 1;
                gbc.gridy = 1;
                JButton viewBalanceButton = new JButton("View Balance");
                viewBalanceButton.addActionListener(e -> showBalancePanel());
                panel.add(viewBalanceButton, gbc);

                gbc.gridy = 2;
                JButton postItemButton = new JButton("Post Item");
                postItemButton.addActionListener(e -> showPostItemPanel());
                panel.add(postItemButton, gbc);

                gbc.gridy = 3;
                JButton searchItemsButton = new JButton("Search Items");
                searchItemsButton.addActionListener(e -> showSearchItemsPanel());
                panel.add(searchItemsButton, gbc);

                gbc.gridy = 4;
                JButton viewItemsButton = new JButton("View All Items");
                viewItemsButton.addActionListener(e -> showViewItemsPanel());
                panel.add(viewItemsButton, gbc);

                gbc.gridy = 5;
                JButton sendMessageButton = new JButton("Send Message");
                sendMessageButton.addActionListener(e -> showSendMessagePanel());
                panel.add(sendMessageButton, gbc);

                gbc.gridy = 6;
                JButton viewMessagesButton = new JButton("View Messages");
                viewMessagesButton.addActionListener(e -> showViewMessagesPanel());
                panel.add(viewMessagesButton, gbc);

                gbc.gridy = 7;
                JButton profileButton = new JButton("Update Profile");
                profileButton.addActionListener(e -> showProfilePanel());
                panel.add(profileButton, gbc);

                gbc.gridy = 8;
                JButton logoutButton = new JButton("Logout");
                logoutButton.addActionListener(e -> {
                    client.logout();
                    showLoginPanel();
                });
                panel.add(logoutButton, gbc);
            } else {
                gbc.gridwidth = 1;
                gbc.gridy = 1;
                JButton loginButton = new JButton("Login");
                loginButton.addActionListener(e -> showLoginPanel());
                panel.add(loginButton, gbc);

                gbc.gridy = 2;
                JButton signupButton = new JButton("Create Account");
                signupButton.addActionListener(e -> showSignupPanel());
                panel.add(signupButton, gbc);
            }

            return panel;
        }

        private JPanel createPostItemPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            JLabel titleLabel = new JLabel("Post New Item");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(titleLabel, gbc);

            gbc.gridwidth = 1;
            gbc.gridy = 1;
            panel.add(new JLabel("Item Name:"), gbc);
            gbc.gridx = 1;
            JTextField nameField = new JTextField(30);
            panel.add(nameField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Price:"), gbc);
            gbc.gridx = 1;
            JTextField priceField = new JTextField(30);
            panel.add(priceField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new JLabel("Description:"), gbc);
            gbc.gridx = 1;
            JTextArea descriptionArea = new JTextArea(5, 30);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(descriptionArea);
            panel.add(scrollPane, gbc);

            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            JButton postButton = new JButton("Post Item");
            postButton.addActionListener(e -> {
                try {
                    String name = nameField.getText();
                    double price = Double.parseDouble(priceField.getText());
                    String description = descriptionArea.getText();
                    if (client.postItem(name, price, description)) {
                        JOptionPane.showMessageDialog(mainFrame, "Item posted successfully!");
                        showMainMenuPanel();
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Error in posting item!");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Please enter a valid price.");
                }
            });
            panel.add(postButton, gbc);

            gbc.gridy = 5;
            JButton backButton = new JButton("Back to Menu");
            backButton.addActionListener(e -> showMainMenuPanel());
            panel.add(backButton, gbc);

            return panel;
        }

        private JPanel createSearchItemsPanel() {
            JPanel panel = new JPanel(new BorderLayout());

            JPanel searchPanel = new JPanel(new FlowLayout());
            JTextField searchField = new JTextField(20);
            JButton searchButton = new JButton("Search");
            searchPanel.add(searchField);
            searchPanel.add(searchButton);

            JTextArea resultsArea = new JTextArea();
            resultsArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(resultsArea);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton backButton = new JButton("Back to Menu");
            backButton.addActionListener(e -> showMainMenuPanel());
            buttonPanel.add(backButton);

            searchButton.addActionListener(e -> {
                try {
                    String searchTerm = searchField.getText();
                    ArrayList<Item> items = client.searchItems(searchTerm);
                    resultsArea.setText("");
                    if (items.isEmpty()) {
                        resultsArea.append("No items found.\n");
                    } else {
                        for (int i = 0; i < items.size(); i++) {
                            Item item = items.get(i);
                            resultsArea.append((i + 1) + ". " + item.getItemName() + " - $" + item.getPrice() +
                                    " - Seller: " + item.getOwner().getName() + "\n");
                            resultsArea.append("   Description: " + item.getDescription() + "\n");

                            // Add buy button for each item
                            JButton buyButton = new JButton("Buy");
                            buyButton.addActionListener(ev -> {
                                if (client.currentUser == null) {
                                    JOptionPane.showMessageDialog(mainFrame, "Please log in to buy items.");
                                    return;
                                }

                                int confirm = JOptionPane.showConfirmDialog(mainFrame,
                                        "Are you sure you want to buy " + item.getItemName() + " for $" + item.getPrice() + "?",
                                        "Confirm Purchase",
                                        JOptionPane.YES_NO_OPTION);

                                if (confirm == JOptionPane.YES_OPTION) {
                                    try {
                                        if (client.buyItem(item.getOwner().getName(), item.getItemName(), item.getPrice())) {
                                            JOptionPane.showMessageDialog(mainFrame, "Purchase successful!");
                                            // Refresh the search results
                                            searchButton.doClick();
                                        } else {
                                            JOptionPane.showMessageDialog(mainFrame, "Purchase failed. Please check your balance.");
                                        }
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(mainFrame, "Error purchasing item: " + ex.getMessage());
                                    }
                                }
                            });
                            buttonPanel.add(buyButton);
                            resultsArea.append("\n");
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Error searching items: " + ex.getMessage());
                }
            });

            panel.add(searchPanel, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel createViewItemsPanel() {
            JPanel panel = new JPanel(new BorderLayout());

            JTextArea itemsArea = new JTextArea();
            itemsArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(itemsArea);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton refreshButton = new JButton("Refresh");
            JButton backButton = new JButton("Back to Menu");
            buttonPanel.add(refreshButton);
            buttonPanel.add(backButton);

            refreshButton.addActionListener(e -> {
                try {
                    ArrayList<Item> items = client.viewItems();
                    itemsArea.setText("");
                    if (items.isEmpty()) {
                        itemsArea.append("No items found.\n");
                    } else {
                        for (Item item : items) {
                            itemsArea.append(item.getItemName() + " - $" + item.getPrice() +
                                    " - " + item.getDescription() + " - " + item.isForSale() + "\n\n");
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Error viewing items: " + ex.getMessage());
                }
            });

            backButton.addActionListener(e -> showMainMenuPanel());

            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel createSendMessagePanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            JLabel titleLabel = new JLabel("Send Message");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(titleLabel, gbc);

            gbc.gridwidth = 1;
            gbc.gridy = 1;
            panel.add(new JLabel("To:"), gbc);
            gbc.gridx = 1;
            JTextField receiverField = new JTextField(20);
            panel.add(receiverField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Message:"), gbc);
            gbc.gridx = 1;
            JTextArea messageArea = new JTextArea(5, 20);
            messageArea.setLineWrap(true);
            JScrollPane scrollPane = new JScrollPane(messageArea);
            panel.add(scrollPane, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            JButton sendButton = new JButton("Send");
            sendButton.addActionListener(e -> {
                String receiver = receiverField.getText();
                String message = messageArea.getText();
                client.sendMessage(receiver, message);
                JOptionPane.showMessageDialog(mainFrame, "Message sent successfully!");
                showMainMenuPanel();
            });
            panel.add(sendButton, gbc);

            gbc.gridy = 4;
            JButton backButton = new JButton("Back to Menu");
            backButton.addActionListener(e -> showMainMenuPanel());
            panel.add(backButton, gbc);

            return panel;
        }

        private JPanel createViewMessagesPanel() {
            JPanel panel = new JPanel(new BorderLayout());

            JTextArea messagesArea = new JTextArea();
            messagesArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(messagesArea);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton refreshButton = new JButton("Refresh");
            JButton backButton = new JButton("Back to Menu");
            buttonPanel.add(refreshButton);
            buttonPanel.add(backButton);

            refreshButton.addActionListener(e -> {
                HashMap<String, ArrayList<String>> messages = client.getMessageHistory();
                messagesArea.setText("");
                if (messages.isEmpty()) {
                    messagesArea.append("No messages.\n");
                } else {
                    for (String partner : messages.keySet()) {
                        messagesArea.append("- " + partner + ":\n");
                        for (String msg : messages.get(partner)) {
                            messagesArea.append("  " + msg + "\n");
                        }
                        messagesArea.append("\n");
                    }
                }
            });

            backButton.addActionListener(e -> showMainMenuPanel());

            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel createProfilePanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            JLabel titleLabel = new JLabel("Update Profile");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(titleLabel, gbc);

            gbc.gridwidth = 1;
            gbc.gridy = 1;
            JButton changeUsernameButton = new JButton("Change Username");
            changeUsernameButton.addActionListener(e -> {
                String newUsername = JOptionPane.showInputDialog(mainFrame, "Enter new username:");
                if (newUsername != null && !newUsername.isEmpty()) {
                    client.changeUsername(newUsername);
                    JOptionPane.showMessageDialog(mainFrame, "Username updated successfully!");
                }
            });
            panel.add(changeUsernameButton, gbc);

            gbc.gridy = 2;
            JButton changePasswordButton = new JButton("Change Password");
            changePasswordButton.addActionListener(e -> {
                String newPassword = JOptionPane.showInputDialog(mainFrame, "Enter new password:");
                if (newPassword != null && !newPassword.isEmpty()) {
                    client.changePassword(newPassword);
                    JOptionPane.showMessageDialog(mainFrame, "Password updated successfully!");
                }
            });
            panel.add(changePasswordButton, gbc);

            gbc.gridy = 3;
            JButton changeEmailButton = new JButton("Change Email");
            changeEmailButton.addActionListener(e -> {
                String newEmail = JOptionPane.showInputDialog(mainFrame, "Enter new email:");
                if (newEmail != null && !newEmail.isEmpty()) {
                    client.changeEmail(newEmail);
                    JOptionPane.showMessageDialog(mainFrame, "Email updated successfully!");
                }
            });
            panel.add(changeEmailButton, gbc);

            gbc.gridy = 4;
            JButton deleteAccountButton = new JButton("Delete Account");
            deleteAccountButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(mainFrame,
                        "Are you sure you want to delete your account? This action cannot be undone.",
                        "Confirm Account Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (client.deleteAccount()) {
                        JOptionPane.showMessageDialog(mainFrame, "Account deleted successfully.");
                        showLoginPanel();
                    }
                }
            });
            panel.add(deleteAccountButton, gbc);

            gbc.gridy = 5;
            JButton backButton = new JButton("Back to Menu");
            backButton.addActionListener(e -> showMainMenuPanel());
            panel.add(backButton, gbc);

            return panel;
        }

        private JPanel createBalancePanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            JLabel balanceLabel;
            if (client.currentUser != null) {
                balanceLabel = new JLabel("Current Balance: $" + client.currentUser.getBalance());
            } else {
                balanceLabel = new JLabel("Please log in to view your balance");
            }
            balanceLabel.setFont(new Font("Arial", Font.BOLD, 20));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(balanceLabel, gbc);

            if (client.currentUser != null) {
                gbc.gridwidth = 1;
                gbc.gridy = 1;
                JButton addButton = new JButton("Add Money");
                addButton.addActionListener(e -> {
                    String amountStr = JOptionPane.showInputDialog(mainFrame, "Enter amount to add:");
                    if (amountStr != null && !amountStr.isEmpty()) {
                        try {
                            double amount = Double.parseDouble(amountStr);
                            if (client.addBalance(amount)) {
                                balanceLabel.setText("Current Balance: $" + client.currentUser.getBalance());
                                JOptionPane.showMessageDialog(mainFrame, "Balance updated successfully!");
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(mainFrame, "Please enter a valid amount.");
                        }
                    }
                });
                panel.add(addButton, gbc);

                gbc.gridx = 1;
                JButton withdrawButton = new JButton("Withdraw Money");
                withdrawButton.addActionListener(e -> {
                    String amountStr = JOptionPane.showInputDialog(mainFrame, "Enter amount to withdraw:");
                    if (amountStr != null && !amountStr.isEmpty()) {
                        try {
                            double amount = Double.parseDouble(amountStr);
                            if (client.withdrawBalance(amount)) {
                                balanceLabel.setText("Current Balance: $" + client.currentUser.getBalance());
                                JOptionPane.showMessageDialog(mainFrame, "Balance updated successfully!");
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(mainFrame, "Please enter a valid amount.");
                        }
                    }
                });
                panel.add(withdrawButton, gbc);
            }

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            JButton backButton = new JButton("Back to Menu");
            backButton.addActionListener(e -> showMainMenuPanel());
            panel.add(backButton, gbc);

            return panel;
        }

        private void showLoginPanel() {
            cardLayout.show(mainPanel, "LOGIN");
        }

        private void showSignupPanel() {
            cardLayout.show(mainPanel, "SIGNUP");
        }

        private void showMainMenuPanel() {
            // Remove the old main menu panel if it exists
            Component[] components = mainPanel.getComponents();
            for (Component component : components) {
                if (component.getName() != null && component.getName().equals("MAIN_MENU")) {
                    mainPanel.remove(component);
                    break;
                }
            }

            // Create and add the new main menu panel
            JPanel mainMenuPanel = createMainMenuPanel();
            mainMenuPanel.setName("MAIN_MENU");
            mainPanel.add(mainMenuPanel, "MAIN_MENU");

            // Show the panel
            cardLayout.show(mainPanel, "MAIN_MENU");
        }

        private void showPostItemPanel() {
            cardLayout.show(mainPanel, "POST_ITEM");
        }

        private void showSearchItemsPanel() {
            cardLayout.show(mainPanel, "SEARCH_ITEMS");
        }

        private void showViewItemsPanel() {
            cardLayout.show(mainPanel, "VIEW_ITEMS");
        }

        private void showSendMessagePanel() {
            cardLayout.show(mainPanel, "SEND_MESSAGE");
        }

        private void showViewMessagesPanel() {
            cardLayout.show(mainPanel, "VIEW_MESSAGES");
        }

        private void showProfilePanel() {
            cardLayout.show(mainPanel, "PROFILE");
        }

        private void showBalancePanel() {
            // Remove the old balance panel if it exists
            Component[] components = mainPanel.getComponents();
            for (Component component : components) {
                if (component.getName() != null && component.getName().equals("BALANCE")) {
                    mainPanel.remove(component);
                    break;
                }
            }

            // Create and add the new balance panel
            JPanel balancePanel = createBalancePanel();
            balancePanel.setName("BALANCE");
            mainPanel.add(balancePanel, "BALANCE");

            // Show the panel
            cardLayout.show(mainPanel, "BALANCE");
        }

        public void show() {
            mainFrame.setVisible(true);
        }
    }
}

