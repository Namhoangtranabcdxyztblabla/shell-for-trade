import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * This is the ClientHandler class of the Project. It calls the database methods to run the project
 *
 * @author Anchit, Nam, Terry, Garv
 * @version April 20 2025
 *
 */


public class ClientHandler implements Runnable {
    // use to handle all clients
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    User currentUser;
    private final Database database;
    private final MessageDatabase messageDatabase;
    private final Object gatekeeper = new Object();
    /**
     * Constructs a new ClientHandler to manage a client connection.
     * Sets up the input and output streams and adds this handler to the list of active handlers.
     *
     * @param socket The client socket connection
     * @param database The user and item database
     * @param messageDatabase The message database
     */
    public ClientHandler(Socket socket, Database database, MessageDatabase messageDatabase) {
        this.messageDatabase = messageDatabase;
        this.database = database;
        try {
            this.socket = socket;
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());
            // use synchronize to prevent from race conditions
            synchronized (gatekeeper) {
                clientHandlers.add(this);
                System.out.println("New client connected. Total clients: " + clientHandlers.size());
            }
        } catch (IOException e) {
            closeEverything(socket, output, input);
        }
    }


    /**
     * Removes this ClientHandler from the list of active handlers.
     * This method is synchronized to prevent concurrent modification issues.
     */
    public synchronized void removeClientHandler() {
        clientHandlers.remove(this);
    }

    /**
     * Closes all resources associated with this client connection.
     * Logs out the user if they are currently logged in.
     *
     * @param socket The client socket to close
     * @param output The output stream to close
     * @param input The input stream to close
     */
    private void closeEverything(Socket socket, ObjectOutputStream output, ObjectInputStream input) {
        // Log out user if they're logged in
        if (currentUser != null) {
            database.logOut(currentUser);
            currentUser = null;
        }
        //remove the clientHandler from the static list
        removeClientHandler();
        //close everything
        try {
            if (output != null) {
                output.flush();
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The main run method for the client handler thread.
     * Continuously processes commands from the client until the connection is closed.
     */
    @Override
    public void run() {
        try {
            // Process client commands until disconnect
            while (socket.isConnected()) {
                // Read command from the client
                Object commandObj = input.readObject();

                if (commandObj instanceof String) {
                    String command = (String) commandObj;
                    processCommand(command);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Error reading command: " + e.getMessage());
        }
    }

    /**
     * Processes a command received from the client.
     * Delegates to the appropriate handler method based on the command.
     *
     * @param command The command string received from the client
     * @throws IOException If there is an error in communication
     */
    public void processCommand(String command) throws IOException {
        try {
            System.out.println("Processing command: " + command);

            switch (command) {
                case "login":
                    handleLogin();
                    break;
                case "logout":
                    handleLogout();
                    break;
                case "createAccount":
                    handleCreateAccount();
                    break;
                case "postItem":
                    handlePostItem();
                    break;
                case "search":
                    handleSearchItems();
                    break;
                case "viewItems":
                    handleViewItems();
                    break;
                case "sendMessage":
                    handleSendMessage();
                    break;
                case "getMessageHistory":
                    handleGetMessageHistory();
                    break;
                case "buyItem":
                    handleBuyItem();
                    break;
                case "changeUsername":
                    handleChangeUsername();
                    break;
                case "changePassword":
                    handleChangePassword();
                    break;
                case "changeEmail":
                    handleChangeEmail();
                    break;
                case "deleteAccount":
                    handleDeleteAccount();
                    break;
                case "withdrawBalance":
                    handleWithdrawBalance();
                    break;
                case "addBalance":
                    handleAddBalance();
                    break;
                default:
                    output.writeObject("ERROR: Unknown command " + command);
                    output.flush();
            }
        } catch (Exception e) {
            System.out.println("Error processing command: " + e.getMessage());
            output.writeObject("ERROR: " + e.getMessage());
            output.flush();
        }
    }

    /**
     * Handles the addBalance command from the client.
     * Adds the specified amount to the current user's balance.
     *
     * @throws IOException If there is an error in communication
     * @throws ClassNotFoundException If there is an error deserializing objects
     */
    public void handleAddBalance() throws IOException, ClassNotFoundException {
        double amount = (double) input.readObject();

        // Check if amount is valid
        if (amount <= 0) {
            output.writeObject("Failure: Amount must be positive");
            output.flush();
            return;
        }

        // Update balance
        currentUser.addBalance(amount);
        database.writeToFile(); // Save changes

        output.writeObject("Success: Added $" + amount + ". New balance: $" + currentUser.getBalance());
        output.flush();
    }


    /**
     * Handles the withdrawBalance command from the client.
     * Withdraws the specified amount from the current user's balance.
     *
     * @throws IOException If there is an error in communication
     * @throws ClassNotFoundException If there is an error deserializing objects
     */
    public void handleWithdrawBalance() throws IOException, ClassNotFoundException {
        double amount = (double) input.readObject();

        // Check if withdrawal amount is valid
        if (amount <= 0) {
            output.writeObject("Failure: Withdrawal amount must be positive");
            output.flush();
            return;
        }

        // Check if user has sufficient balance
        if (currentUser.getBalance() < amount) {
            output.writeObject("Failure: Insufficient balance");
            output.flush();
            return;
        }

        // Update balance
        currentUser.setBalance(currentUser.getBalance() - amount);
        database.writeToFile(); // Save changes

        output.writeObject("Success: Withdrew $" + amount + ". New balance: $" + currentUser.getBalance());
        output.flush();
    }

    /**
     * Handles the login command from the client.
     * Attempts to authenticate a user with the provided credentials.
     *
     * @throws IOException If there is an error in communication
     * @throws ClassNotFoundException If there is an error deserializing objects
     */
    public void handleLogin() throws IOException, ClassNotFoundException {
        String emailOrUsername = (String) input.readObject();
        String password = (String) input.readObject();
        try {
            boolean success = database.login(emailOrUsername, password);
            if (success) {
                // Set the current user based on login identity
                if (emailOrUsername.contains("@")) {
                    currentUser = database.findByEmail(emailOrUsername);
                } else {
                    currentUser = database.findByUsername(emailOrUsername);
                }
                output.writeObject("Success");
                output.writeObject(currentUser);
                output.flush();
            } else {
                output.writeObject("FAILURE: Login failed");
                output.flush();
            }
        } catch (InvalidAccountOperationException e) {
            output.writeObject("Failure: " + e.getMessage());
//            output.writeObject(database.findByEmail(emailOrUsername).getPassword()); //for testing
            output.flush();
        }
    }

    /**
     * Handles the logout command from the client.
     * Logs out the current user if one is logged in.
     *
     * @throws IOException If there is an error in communication
     */
    public void handleLogout() throws IOException {
        if (currentUser != null) {
            database.logOut(currentUser);
            currentUser = null;
            output.writeObject("Success");
        } else {
            output.writeObject("Failure: No user is currently logged in");
        }
        output.flush();
    }

    /**
     * Handles the createAccount command from the client.
     * Creates a new user account with the provided details.
     *
     * @throws IOException If there is an error in communication
     * @throws ClassNotFoundException If there is an error deserializing objects
     */
    public void handleCreateAccount() throws IOException, ClassNotFoundException {
        String username = (String) input.readObject();
        String email = (String) input.readObject();
        String password = (String) input.readObject();
        double balance = (double) input.readObject();
        try {
            database.createUser(username, email, password, balance);
            output.writeObject("Success");
        } catch (InvalidAccountOperationException e) {
            output.writeObject("Cannot create account: " + e.getMessage());
        }
        output.flush();
    }

    /**
     * Handles the postItem command from the client.
     * Creates a new item listing in the marketplace.
     *
     * @throws IOException If there is an error in communication
     * @throws ClassNotFoundException If there is an error deserializing objects
     */
    public void handlePostItem() throws IOException, ClassNotFoundException {
        String itemName = (String) input.readObject();
        double price = (double) input.readObject();
        String description = (String) input.readObject();

        try {
            boolean success = database.createItem(currentUser, itemName, price, description);
            if (success) {
                output.writeObject("Success");
                database.writeToFile(); //save data
            } else {
                output.writeObject("FAILURE: Failed to post item");
            }
        } catch (Exception e) {
            output.writeObject("FAILURE: " + e.getMessage());
        }
        output.flush();
    }

    /**
     * Handles the search command from the client.
     * Searches for items matching the specified search term.
     *
     * @throws IOException If there is an error in communication
     * @throws ClassNotFoundException If there is an error deserializing objects
     */
    public void handleSearchItems() throws IOException, ClassNotFoundException {
        String searchTerm = (String) input.readObject();
        ArrayList<Item> items = database.getItemsFromName(searchTerm);
        output.writeObject(items);
        output.flush();
    }

    public void handleViewItems() throws IOException {
        // This will return all items in the database
        ArrayList<Item> allItems = database.allItemList;
        output.writeObject(allItems);
        output.flush();
    }

    /**
     * Handles the viewItems command from the client.
     * Returns a list of all items in the marketplace.
     *
     * @throws IOException If there is an error in communication
     */
    public void handleSendMessage() throws IOException, ClassNotFoundException {
        String receiverUsername = (String) input.readObject();
        String messageContent = (String) input.readObject();

        User receiver = database.findByUsername(receiverUsername);
        if (receiver == null) {
            output.writeObject("FAILURE: Receiver not found");
            output.flush();
            return;
        }
        boolean success = messageDatabase.sendMessage(currentUser.getName(),
                receiverUsername, messageContent);
        if (success) {
            output.writeObject("Success");
        } else {
            output.writeObject("Failure: Failed to send message");
        }
        output.flush();
    }

    /**
     * Handles the getMessageHistory command from the client.
     * Retrieves the message history for the current user.
     *
     * @throws IOException If there is an error in communication
     */
    public void handleGetMessageHistory() throws IOException {
        if (currentUser == null) {
            output.writeObject("FAILURE: User not logged in");
            output.flush();
            return;
        }
        // Get all users who message the currentUser
        ArrayList<String> messagePartners = messageDatabase.userMessageList.get(currentUser.getName());
        HashMap<String, ArrayList<String>> allMessages = new HashMap<>();
        if (messagePartners != null) {
            for (String partner : messagePartners) {
                allMessages.put(partner,
                        messageDatabase.getMessageHistory(currentUser.getName(), partner));
            }
        }
        output.writeObject(allMessages);
        output.flush();
    }

    /**
     * Handles the buyItem command from the client.
     * Processes a transaction between the current user and a seller.
     *
     * @throws IOException If there is an error in communication
     * @throws ClassNotFoundException If there is an error deserializing objects
     */
    public void handleBuyItem() throws IOException, ClassNotFoundException {
        String sellerName = (String) input.readObject();
        String itemName = (String) input.readObject();
        double price = (double) input.readObject();

        //Find the seller
        User seller = database.findByUsername(sellerName);
        if (seller == null) {
            output.writeObject("Failure: Seller not found");
            output.flush();
            return;
        }

        //Find the item
        ArrayList<Item> sellerItems = database.getItemsFromOwner(seller);
        Item itemToBuy = null;
        for (Item item: sellerItems) {
            if (item.getItemName().equals(itemName) && item.getPrice() == price) {
                itemToBuy = item;
                break;
            }
        }
        if (itemToBuy == null) {
            output.writeObject("Failure: Item not found");
            output.flush();
            return;
        }

        String result = database.transaction(currentUser, seller, itemToBuy);
        if (result.equals("Transaction occurs successfully")) {
            output.writeObject("Success: " + result);
        } else {
            output.writeObject("Failure: " + result);
        }
        output.flush();
    }

    /**
     * Handles the changeUsername command from the client.
     * Changes the username of the current user.
     *
     * @throws IOException If there is an error in communication
     * @throws ClassNotFoundException If there is an error deserializing objects
     */
    public void handleChangeUsername() throws IOException, ClassNotFoundException {
        String newUsername = (String) input.readObject();

        try {
            if (database.findByUsername(newUsername) != null) {
                output.writeObject("Failure: Username already exists");
                output.flush();
            } else {
                currentUser.setName(newUsername);
                database.writeToFile();
                output.writeObject("Success");
            }
        } catch (InvalidAccountOperationException e) {
            output.writeObject("Failure: " + e.getMessage());
        }
        output.flush();
    }

    /**
     * Handles the changePassword command from the client.
     * Changes the password of the current user.
     *
     * @throws IOException If there is an error in communication
     * @throws ClassNotFoundException If there is an error deserializing objects
     */
    public void handleChangePassword() throws IOException, ClassNotFoundException {
        String newPassword = (String) input.readObject();

        try {
            currentUser.setPassword(newPassword);
            database.writeToFile(); // Save changes
            output.writeObject("Success");
        } catch (InvalidAccountOperationException e) {
            output.writeObject("Failure: " + e.getMessage());
        }
        output.flush();
    }

    /**
     * Handles the changeEmail command from the client.
     * Changes the email address of the current user.
     *
     * @throws IOException If there is an error in communication
     * @throws ClassNotFoundException If there is an error deserializing objects
     */
    public void handleChangeEmail() throws IOException, ClassNotFoundException {
        String newEmail = (String) input.readObject();
        try {
            // Check if email already exists
            if (database.findByEmail(newEmail) != null) {
                output.writeObject("Failure: Email already exists");
            } else {
                // We need to update the key in the hashmap
                String oldEmail = currentUser.getEmail();
                currentUser.setEmail(newEmail);

                // Update the user in the database
                database.deleteUser(currentUser);
                database.allUserList.put(newEmail, currentUser);
                database.writeToFile(); // Save changes

                output.writeObject("Success");
            }
        } catch (InvalidAccountOperationException e) {
            output.writeObject("Failure: " + e.getMessage());
        }
        output.flush();
    }

    /**
     * Handles the deleteAccount command from the client.
     * Deletes the current user's account from the system.
     *
     * @throws IOException If there is an error in communication
     */
    public void handleDeleteAccount() throws IOException {
        database.deleteUser(currentUser);
        database.writeToFile(); //Save changes

        currentUser = null;
        output.writeObject("Success");
        output.flush();
    }
}
