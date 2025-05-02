import java.util.ArrayList;
import java.util.Scanner;

/**
 * Terminal-based interface for the Marketplace application.
 * This class provides a text-based user interface for interacting with the marketplace system.
 * Users can register, login, post items for sale, search for items, send messages, and more.
 */
public class MarketplaceTerminal {
    private Scanner scanner;
    private Database database;
    private MessageDatabase messageDatabase;
    private User currentUser;
    private boolean running;

    /**
     * Constructs a new MarketplaceTerminal instance.
     * Initializes the scanner, database, and message database.
     */
    public MarketplaceTerminal() {
        scanner = new Scanner(System.in);
        database = new Database();
        messageDatabase = new MessageDatabase();
        running = true;
        currentUser = null;
    }

    /**
     * Starts the marketplace terminal application.
     * Shows the main menu and handles user input until the application is exited.
     */
    public void start() {
        System.out.println("Welcome to the Marketplace Application!");

        while (running) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }

        // Save all data before exiting
        database.writeToFile();
        try {
            messageDatabase.saveFile();
        } catch (Exception e) {
            System.out.println("Error saving message data: " + e.getMessage());
        }

        System.out.println("Thank you for using the Marketplace Application. Goodbye!");
        scanner.close();
    }

    /**
     * Displays the login menu for unauthenticated users.
     * Options include login, register, and exit.
     */
    private void showLoginMenu() {
        System.out.println("\n===== LOGIN MENU =====");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            case 3:
                running = false;
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    /**
     * Displays the main menu for authenticated users.
     * Options include marketplace actions, account management, and logout.
     */
    private void showMainMenu() {
        System.out.println("\n===== MAIN MENU =====");
        System.out.println("Welcome, " + currentUser.getName() + "! Balance: $" + String.format("%.2f", currentUser.getBalance()));
        System.out.println("1. Browse items for sale");
        System.out.println("2. Search for items");
        System.out.println("3. Post an item for sale");
        System.out.println("4. Manage my listings");
        System.out.println("5. Messages");
        System.out.println("6. Account settings");
        System.out.println("7. Logout");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                browseItems();
                break;
            case 2:
                searchItems();
                break;
            case 3:
                postItem();
                break;
            case 4:
                manageListings();
                break;
            case 5:
                messages();
                break;
            case 6:
                accountSettings();
                break;
            case 7:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    /**
     * Handles the user login process.
     * Prompts for email/username and password, then attempts to authenticate.
     */
    private void login() {
        System.out.println("\n===== LOGIN =====");
        System.out.print("Enter your email or username: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine().trim();

        try {
            boolean success = database.login(email, password);
            if (success) {
                if (email.contains("@")) {
                    currentUser = database.findByEmail(email);
                } else {
                    currentUser = database.findByUsername(email);
                }
                System.out.println("Login successful! Welcome, " + currentUser.getName() + "!");
            }
        } catch (InvalidAccountOperationException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    /**
     * Handles the user registration process.
     * Collects new user details and creates a new account.
     */
    private void register() {
        System.out.println("\n===== REGISTER =====");

        System.out.print("Enter a username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter your email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter a password (must be at least 8 characters with uppercase, lowercase, and numbers): ");
        String password = scanner.nextLine().trim();

        System.out.print("Enter initial account balance: $");
        double balance = getDoubleInput();

        try {
            database.createUser(username, email, password, balance);
            System.out.println("Registration successful! Please login to continue.");
        } catch (InvalidAccountOperationException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Logs out the current user.
     */
    private void logout() {
        if (currentUser != null) {
            database.logOut(currentUser);
            currentUser = null;
            System.out.println("You have been logged out successfully.");
        }
    }

    /**
     * Displays all items for sale by other users.
     */
    private void browseItems() {
        System.out.println("\n===== BROWSE ITEMS FOR SALE =====");

        ArrayList<Item> allItems = new ArrayList<>();
        for (Item item : database.allItemList) {
            if (item.isForSale() && !item.getOwner().equals(currentUser)) {
                allItems.add(item);
            }
        }

        if (allItems.isEmpty()) {
            System.out.println("No items found for sale.");
            return;
        }

        displayItems(allItems);

        System.out.println("\n1. Purchase an item");
        System.out.println("2. Message a seller");
        System.out.println("3. Return to main menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                purchaseItem(allItems);
                break;
            case 2:
                messageSeller(allItems);
                break;
            case 3:
                // Return to main menu
                break;
            default:
                System.out.println("Invalid choice. Returning to main menu.");
        }
    }

    /**
     * Searches for items by name.
     */
    private void searchItems() {
        System.out.println("\n===== SEARCH ITEMS =====");
        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine().trim();

        ArrayList<Item> foundItems = database.getItemsFromName(searchTerm);

        if (foundItems.isEmpty()) {
            System.out.println("No items found matching your search.");
            return;
        }

        System.out.println("\nSearch results:");
        displayItems(foundItems);

        System.out.println("\n1. Purchase an item");
        System.out.println("2. Message a seller");
        System.out.println("3. Return to main menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                purchaseItem(foundItems);
                break;
            case 2:
                messageSeller(foundItems);
                break;
            case 3:
                // Return to main menu
                break;
            default:
                System.out.println("Invalid choice. Returning to main menu.");
        }
    }

    /**
     * Creates a new item for sale.
     */
    private void postItem() {
        System.out.println("\n===== POST ITEM FOR SALE =====");

        System.out.print("Enter item name: ");
        String itemName = scanner.nextLine().trim();

        System.out.print("Enter item description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Enter price ($): ");
        double price = getDoubleInput();

        try {
            boolean success = database.createItem(currentUser, itemName, price, description);
            if (success) {
                System.out.println("Item posted successfully!");
            } else {
                System.out.println("Failed to post item.");
            }
        } catch (Exception e) {
            System.out.println("Error posting item: " + e.getMessage());
        }
    }

    /**
     * Manages the current user's listings.
     */
    private void manageListings() {
        System.out.println("\n===== MANAGE MY LISTINGS =====");

        ArrayList<Item> myItems = database.getItemsFromOwner(currentUser);

        if (myItems.isEmpty()) {
            System.out.println("You don't have any items listed for sale.");
            return;
        }

        System.out.println("Your listings:");
        displayItems(myItems);

        System.out.println("\n1. Remove an item");
        System.out.println("2. Return to main menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        if (choice == 1) {
            System.out.print("Enter the number of the item to remove: ");
            int itemIndex = getIntInput() - 1;

            if (itemIndex >= 0 && itemIndex < myItems.size()) {
                database.deleteItem(myItems.get(itemIndex));
                System.out.println("Item removed successfully.");
            } else {
                System.out.println("Invalid item number.");
            }
        }
    }

    /**
     * Handles messaging functionality.
     */
    private void messages() {
        System.out.println("\n===== MESSAGES =====");

        // Get all users this user has messaged with
        ArrayList<String> contacts = new ArrayList<>();
        if (messageDatabase.userMessageList.containsKey(currentUser.getName())) {
            contacts = messageDatabase.userMessageList.get(currentUser.getName());
        }

        if (contacts.isEmpty()) {
            System.out.println("You don't have any message history.");
            System.out.println("You can message users when browsing or searching for items.");
            return;
        }

        System.out.println("Select a user to view conversation:");
        for (int i = 0; i < contacts.size(); i++) {
            System.out.println((i + 1) + ". " + contacts.get(i));
        }
        System.out.println((contacts.size() + 1) + ". Return to main menu");

        System.out.print("Enter your choice: ");
        int choice = getIntInput() - 1;

        if (choice >= 0 && choice < contacts.size()) {
            String otherUser = contacts.get(choice);
            viewConversation(otherUser);
        } else if (choice != contacts.size()) {
            System.out.println("Invalid choice.");
        }
    }

    /**
     * Views and allows sending messages in a conversation with another user.
     *
     * @param otherUser the username of the other user in the conversation
     */
    private void viewConversation(String otherUser) {
        System.out.println("\n===== CONVERSATION WITH " + otherUser + " =====");

        // Display message history
        ArrayList<String> history = messageDatabase.getMessageHistory(currentUser.getName(), otherUser);

        if (history.isEmpty()) {
            System.out.println("No messages yet.");
        } else {
            for (String message : history) {
                System.out.println(message);
            }
        }

        System.out.println("\n1. Send a message");
        System.out.println("2. Return to messages");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        if (choice == 1) {
            System.out.print("Enter your message: ");
            String message = scanner.nextLine().trim();

            if (!message.isEmpty()) {
                boolean sent = messageDatabase.sendMessage(currentUser.getName(), otherUser, message);
                if (sent) {
                    System.out.println("Message sent successfully.");
                } else {
                    System.out.println("Failed to send message.");
                }
            } else {
                System.out.println("Message cannot be empty.");
            }

            // Show the conversation again
            viewConversation(otherUser);
        }
    }

    /**
     * Handles account settings and management.
     */
    private void accountSettings() {
        System.out.println("\n===== ACCOUNT SETTINGS =====");
        System.out.println("1. Add balance");
        System.out.println("2. Change username");
        System.out.println("3. Change password");
        System.out.println("4. Delete account");
        System.out.println("5. Return to main menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                addBalance();
                break;
            case 2:
                changeUsername();
                break;
            case 3:
                changePassword();
                break;
            case 4:
                deleteAccount();
                break;
            case 5:
                // Return to main menu
                break;
            default:
                System.out.println("Invalid choice. Returning to main menu.");
        }
    }

    /**
     * Allows the user to add balance to their account.
     */
    private void addBalance() {
        System.out.println("\n===== ADD BALANCE =====");
        System.out.println("Current balance: $" + String.format("%.2f", currentUser.getBalance()));
        System.out.print("Enter amount to add: $");

        double amount = getDoubleInput();

        if (amount <= 0) {
            System.out.println("Invalid amount. Please enter a positive value.");
            return;
        }

        currentUser.addBalance(amount);
        System.out.println("Balance updated. New balance: $" + String.format("%.2f", currentUser.getBalance()));
        database.writeToFile();
    }

    /**
     * Allows the user to change their username.
     */
    private void changeUsername() {
        System.out.println("\n===== CHANGE USERNAME =====");
        System.out.println("Current username: " + currentUser.getName());
        System.out.print("Enter new username: ");

        String newUsername = scanner.nextLine().trim();

        try {
            currentUser.setName(newUsername);
            System.out.println("Username updated successfully.");
            database.writeToFile();
        } catch (InvalidAccountOperationException e) {
            System.out.println("Failed to update username: " + e.getMessage());
        }
    }

    /**
     * Allows the user to change their password.
     */
    private void changePassword() {
        System.out.println("\n===== CHANGE PASSWORD =====");
        System.out.print("Enter your current password: ");

        String currentPassword = scanner.nextLine().trim();

        if (!currentPassword.equals(currentUser.getPassword())) {
            System.out.println("Incorrect password.");
            return;
        }

        System.out.print("Enter new password (must be at least 8 characters with uppercase, lowercase, and numbers): ");
        String newPassword = scanner.nextLine().trim();

        try {
            currentUser.setPassword(newPassword);
            System.out.println("Password updated successfully.");
            database.writeToFile();
        } catch (InvalidAccountOperationException e) {
            System.out.println("Failed to update password: " + e.getMessage());
        }
    }

    /**
     * Handles the account deletion process.
     */
    private void deleteAccount() {
        System.out.println("\n===== DELETE ACCOUNT =====");
        System.out.println("WARNING: This action cannot be undone.");
        System.out.print("To confirm deletion, type 'DELETE': ");

        String confirmation = scanner.nextLine().trim();

        if (confirmation.equals("DELETE")) {
            database.deleteUser(currentUser);
            System.out.println("Account deleted successfully.");
            currentUser = null;
            database.writeToFile();
        } else {
            System.out.println("Account deletion cancelled.");
        }
    }

    /**
     * Facilitates the purchase of an item.
     *
     * @param items the list of items from which the user can purchase
     */
    private void purchaseItem(ArrayList<Item> items) {
        System.out.print("Enter the number of the item you want to purchase: ");
        int itemIndex = getIntInput() - 1;

        if (itemIndex < 0 || itemIndex >= items.size()) {
            System.out.println("Invalid item number.");
            return;
        }

        Item selectedItem = items.get(itemIndex);

        if (selectedItem.getPrice() > currentUser.getBalance()) {
            System.out.println("Insufficient balance to purchase this item.");
            return;
        }

        if (!selectedItem.isForSale()) {
            System.out.println("This item is no longer for sale.");
            return;
        }

        System.out.println("\nYou are about to purchase:");
        System.out.println(selectedItem.getItemName() + " - $" + String.format("%.2f", selectedItem.getPrice()));
        System.out.println("Description: " + selectedItem.getDescription());
        System.out.println("From: " + selectedItem.getOwner().getName());

        System.out.print("\nConfirm purchase (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            String transactionResult = database.transaction(currentUser, selectedItem.getOwner(), selectedItem);
            System.out.println(transactionResult);

            if (transactionResult.contains("successfully")) {
                database.writeToFile();
            }
        } else {
            System.out.println("Purchase cancelled.");
        }
    }

    /**
     * Allows the user to send a message to a seller.
     *
     * @param items the list of items whose sellers can be messaged
     */
    private void messageSeller(ArrayList<Item> items) {
        System.out.print("Enter the number of the item whose seller you want to message: ");
        int itemIndex = getIntInput() - 1;

        if (itemIndex < 0 || itemIndex >= items.size()) {
            System.out.println("Invalid item number.");
            return;
        }

        User seller = items.get(itemIndex).getOwner();

        System.out.println("\nSending message to: " + seller.getName());
        System.out.print("Enter your message: ");
        String message = scanner.nextLine().trim();

        if (!message.isEmpty()) {
            boolean sent = messageDatabase.sendMessage(currentUser.getName(), seller.getName(), message);
            if (sent) {
                System.out.println("Message sent successfully.");
                try {
                    messageDatabase.saveFile();
                } catch (Exception e) {
                    System.out.println("Error saving message: " + e.getMessage());
                }
            } else {
                System.out.println("Failed to send message.");
            }
        } else {
            System.out.println("Message cannot be empty. Message not sent.");
        }
    }

    /**
     * Displays a list of items in a formatted manner.
     *
     * @param items the list of items to display
     */
    private void displayItems(ArrayList<Item> items) {
        if (items.isEmpty()) {
            System.out.println("No items to display.");
            return;
        }

        System.out.println("\n-------------------------------------------------------------------------------------------------");
        System.out.printf("%-5s %-25s %-25s %-10s %-30s\n", "No.", "Item Name", "Seller", "Price", "Description");
        System.out.println("-------------------------------------------------------------------------------------------------");

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            System.out.printf("%-5d %-25s %-25s $%-9.2f %-30s\n",
                    (i + 1),
                    truncate(item.getItemName(), 24),
                    truncate(item.getOwner().getName(), 24),
                    item.getPrice(),
                    truncate(item.getDescription(), 29));
        }

        System.out.println("-------------------------------------------------------------------------------------------------");
    }

    /**
     * Helper method to truncate strings for display purposes.
     *
     * @param str the string to truncate
     * @param length the maximum length
     * @return the truncated string
     */
    private String truncate(String str, int length) {
        if (str.length() <= length) {
            return str;
        } else {
            return str.substring(0, length - 3) + "...";
        }
    }

    /**
     * Gets an integer input from the user with error handling.
     *
     * @return the integer entered by the user
     */
    private int getIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    /**
     * Gets a double input from the user with error handling.
     *
     * @return the double entered by the user
     */
    private double getDoubleInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    /**
     * Main method to start the application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        MarketplaceTerminal terminal = new MarketplaceTerminal();
        terminal.start();
    }
}