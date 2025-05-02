import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
/**
 *
 * This is the Database class of the project. It holds all the data
 * for the marketplace and allows users to interact with items and other users
 *
 * @author Anchit, Nam, Terry, Garv
 * @version April 20 2025
 *
 */

public class Database {
    // private HashMap<String,String> userIDPassword; // this was never used
    // private ArrayList<String> allUserEmail; // only used once
    // private ArrayList<String> allUsername;

    public HashMap<String, User> allUserList;
    ArrayList<Item> allItemList;
    private final String allUserFileName = "allUser.txt";
    private final String allItemFileName = "MarketInventory.txt";

    public Database() {
        allUserList = new HashMap<>();
        allItemList = new ArrayList<>();
        loadDatabase();
    }

    /**
     * Loads all user and item data from files into memory.
     * This method reads from two files: allUser.txt for user data and
     * MarketInventory.txt for item data.
     */
    public synchronized void loadDatabase() {
        File userFile = new File(allUserFileName);
        File allItemFile = new File(allItemFileName);

        try (BufferedReader bfr = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                String[] parts = line.split(",");
                String username = parts[0];
                String password = parts[1];
                String email = parts[2];
                double balance = Double.parseDouble(parts[3]);
                allUserList.put(email, new User(username, email, password, balance)); // note:
                // allUserEmail.add(email);
                // userIDPassword.put(email, password);
                // allUsername.add(username);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (BufferedReader bfr = new BufferedReader(new FileReader(allItemFile))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                String[] itemPart = line.split(",");
                String ownerName = itemPart[0];
                String itemName = itemPart[1];
                double price = Double.parseDouble(itemPart[2]);
                String description = itemPart[3];
                boolean forSale = Boolean.parseBoolean(itemPart[4]);
                Item item = new Item(allUserList.get(ownerName), itemName, price, description, forSale);
                allItemList.add(item);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes all current user and item data from memory to files.
     * The method first clears existing file content, then writes all current data.
     *
     * @return true if the write operation was successful, false otherwise
     */
    public synchronized boolean writeToFile() {
        try {
            deleteContentInFile(allUserFileName);
            deleteContentInFile(allItemFileName);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }

        for (User user : allUserList.values()) {
            try (BufferedWriter bfw = new BufferedWriter(new FileWriter(new File(allUserFileName), true))) {
                bfw.write(user.toFileString());
                bfw.newLine();
            } catch (IOException e) {
                System.out.println("Error: IO Exception");
                return false;
            }
        }

        for (Item item : allItemList) {
            try (BufferedWriter bfw = new BufferedWriter(new FileWriter(new File(allItemFileName), true))) {
                bfw.write(item.toFileString());
                bfw.newLine();
            } catch (IOException e) {
                System.out.println("Error: IO Exception");
                return false;
            }
        }
        return true;
    }

    /**
     * Searches and returns items that match the given name (fully or partially).
     * Only returns items that are currently for sale.
     * This method is synchronized to prevent concurrent access issues.
     *
     * @param name the item name to search for
     * @return an ArrayList of items that match the search criteria
     */
    public synchronized ArrayList<Item> getItemsFromName(String name) {
        ArrayList<Item> found = new ArrayList<Item>();
        for (Item item : allItemList) {
            // check if search words contain item names or item name that have the searched
            // word in there.
            if ((item.getItemName().contains(name) || name.contains(item.getItemName())
                    && item.isForSale())) {
                found.add(item);
            }
        }
        return found;
    }

    /**
     * Returns all items owned by a specific user that are currently for sale.
     * This method is synchronized to prevent concurrent access issues.
     *
     * @param owner the user whose items to retrieve
     * @return an ArrayList of items owned by the specified user
     */
    public synchronized ArrayList<Item> getItemsFromOwner(User owner) {
        ArrayList<Item> sellerItemList = new ArrayList<>();
        for (Item i : allItemList) {
            if (i.getOwner().equals(owner) && i.isForSale()) {
                sellerItemList.add(i);
            }
        }
        return sellerItemList;
    }

    /**
     * Validates an email address using a regular expression pattern.
     *
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public boolean checkValidEmail(String email) {
        // Regular expression to match valid email formats
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        // Compile the regex
        Pattern p = Pattern.compile(emailRegex);

        // check the invalid email
        return email != null && p.matcher(email).matches();
    }

    /**
     * Creates a new user account with the provided information.
     * Using this method to sign up a new account.
     * Validates username, email, and password before creating the account.
     * This method is synchronized to prevent concurrent access issues.
     *
     * @param name     the user's name/username
     * @param email    the user's email address (used as userID)
     * @param password the user's password
     * @param balance  the user's initial account balance
     * @throws InvalidAccountOperationException if username already exists, email
     *                                          already exists,
     *                                          or any of the input fields fail
     *                                          validation
     */
    public synchronized void createUser(String name, String email, String password, double balance)
            throws InvalidAccountOperationException { // guess not
        if (findByUsername(name) != null) {
            throw new InvalidAccountOperationException("A user with this username already exists");
        }
        if (allUserList.get(email) != null || findByUsername(name) != null) {
            throw new InvalidAccountOperationException("A user with this email already exists");
        } else {
            if (name == null || name.startsWith(" ") || name.trim().isEmpty()) {
                throw new InvalidAccountOperationException(
                        "Invalid username. Please make sure your username have at least" +
                                " 1 character and does not start with a space.");
            }
            if (!checkValidEmail(email)) {
                throw new InvalidAccountOperationException("Invalid email! Please enter an valid email.");
            }
            if (password == null || password.contains(" ") || password.length() < 8 ||
                    !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") ||
                    !password.matches(".*[0-9].*") || password.trim().isEmpty()) {
                throw new InvalidAccountOperationException(
                        "Invalid password.Password must be at least 8 characters and " +
                                "include at least one uppercase letter, one lowercase letter, and one digit. Password cannot start "
                                +
                                "with a space.");
            }
            User user = new User(name, email, password, balance);
            allUserList.put(email, user);

            try (BufferedWriter bfw = new BufferedWriter(new FileWriter(new File(allUserFileName), true))) {
                bfw.write(user.toString());
                bfw.newLine();
            } catch (IOException e) {
                System.out.println("Error: IO Exception");
            }
        }
    }

    /**
     * Finds and returns a user by their email address.
     *
     * @param email the email address to search for
     * @return the User object if found, null otherwise
     */
    public synchronized User findByEmail(String email) {
        return allUserList.get(email);
    }

    /**
     * Finds and returns a user by their username.
     *
     * @param name the username to search for
     * @return the User object if found, null otherwise
     */
    public synchronized User findByUsername(String name) {
        for (Map.Entry<String, User> set : allUserList.entrySet()) {
            if (set.getValue().getName().equals(name)) {
                return set.getValue();
            }
        }
        return null;
    }

    /**
     * Authenticates a user based on email/username and password.
     * Updates the user's online status to true if login is successful.
     * throws exception with different message for each time login fail
     * This method is synchronized to prevent concurrent access issues.
     *
     * @param email    the user's email or username
     * @param password the user's password
     * @return true if authentication is successful
     * @throws InvalidAccountOperationException if credentials are invalid or the
     *                                          account is already logged in
     */
    public synchronized boolean login(String email, String password) throws InvalidAccountOperationException {
        User user;
        if (email.isEmpty()) {
            throw new InvalidAccountOperationException("Email or Username cannot be empty!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidAccountOperationException("Password cannot be empty");
        }
        boolean isEmail = email.contains("@") && email.contains("."); // check if this is an email or not
        if (isEmail) {
            user = findByEmail(email);
        } else {
            user = findByUsername(email);
        }
        if (user == null) {
            // Using a generic error message for security reasons
            throw new InvalidAccountOperationException("Invalid credentials");
        }
        if (user.isOnlineStatus()) {
            throw new InvalidAccountOperationException("This account is already logged in");
        }
        String correctPassword = user.getPassword();
        if (password.equals(correctPassword)) {
            user.setOnlineStatus(true);
            System.out.println("Login Successfully");
            return true;
        } else {
            throw new InvalidAccountOperationException("Invalid Password! Please try again");
        }
    }

    /**
     * Logs out a user by setting their online status to false.
     * This method is synchronized to prevent concurrent access issues.
     *
     * @param user the user to log out
     */
    public synchronized void logOut(User user) {
        user.setOnlineStatus(false);
        System.out.println("Logout successfully");
    }

    /**
     * Allows a user to post an item for selling.
     *
     * @param owner       The seller who post this item.
     * @param itemName    The name of item.
     * @param price       price of item
     * @param description some description about this item
     *
     * @return true if successfully remove the item
     * @throws InvalidAccountOperationException when the price is invalid
     */
    public synchronized boolean createItem(User owner, String itemName, double price, String description)
            throws Exception {
        if (price <= 0) {
            throw new Exception("Invalid price");
        }
        Item item = new Item(owner, itemName, price, description, true);
        allItemList.add(item);
         try (BufferedWriter bfw = new BufferedWriter(new FileWriter(new File(allItemFileName), true))) {
            bfw.write(item.toFileString());
            bfw.newLine();
         } catch (IOException e) {
            System.out.println("Error: IO Exception");
         }
        return true;
    }

    /**
     * Deletes all content from a specified file.
     *
     * @param fileName the name of the file to clear
     * @throws IOException if an I/O error occurs
     */
    public void deleteContentInFile(String fileName) throws IOException {
        File f = new File(fileName);
        new FileOutputStream(f, false).close();
    }

    /**
     * Removes a user and all their items from the database.
     * This method is synchronized to prevent concurrent access issues.
     *
     * @param user the user to delete
     */
    public synchronized void deleteUser(User user) {
        String userEmail = user.getEmail();
        allUserList.remove(userEmail);
        // allUserEmail.remove(userEmail);
        // allUsername.remove(user.getName());
        allItemList.removeIf(i -> i.getOwner().equals(user));
        // try {
        // deleteContentInFile(allUserFileName);
        // deleteContentInFile(allItemFileName);
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // try (BufferedWriter bfw = new BufferedWriter(new FileWriter(new
        // File(allUserFileName), true))) {
        // for (Map.Entry<String, User> set : allUserList.entrySet()) {
        // bfw.write(set.getValue().toString());
        // bfw.newLine();
        // }
        // } catch (IOException e) {
        // System.out.println("Error: IO Exception");
        // }
        // try (BufferedWriter bfw = new BufferedWriter(new FileWriter(new
        // File(allItemFileName), true))) {
        // for (Item i: allItemList) {
        // bfw.write(i.toFileString());
        // bfw.newLine();
        // }
        // } catch (IOException e) {
        // System.out.println("Error: IO Exception");
        // }

    }

    /**
     * Allows an user to delete an item from their selling list.
     *
     * @param item: The item that is going to be removed
     */
    public synchronized void deleteItem(Item item) {
        allItemList.remove(item);
        System.out.println("Remove the item successfully");
        // try {
        // deleteContentInFile(allItemFileName);
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // try (BufferedWriter bfw = new BufferedWriter(new FileWriter(new
        // File(allItemFileName), true))) {
        // for (Item i: allItemList) {
        // bfw.write(i.toFileString());
        // bfw.newLine();
        // }
        // } catch (IOException e) {
        // System.out.println("Error: IO Exception");
        // }

    }

    // // generic search function - might break if there is null in arraylist -
    // might move to util class later
    // // returns the EXACT object found in array
    // public static <T> T findInArrayList(ArrayList<T> list, T obj) {
    // for (T item : list) {
    // if (item != null && item.equals(obj)) {
    // return item;
    // }
    // }
    // return null;
    // }

    /**
     * A method for a transaction between buyer and seller
     *
     * @param buyer  The buyer of this transaction.
     * @param seller The seller.
     * @param item   the item which is sold
     *
     * @return the string if there is an error in the transaction, null if there
     *         isn't one
     */
    public synchronized String transaction(User buyer, User seller, Item item) {
        if (!allUserList.containsValue(buyer) || !allUserList.containsValue(seller)) {
            return "Seller doesn't exist";
        }
        if (!item.isForSale()) {
            return "Item is not sold now";
        }
        if (item.getPrice() > buyer.getBalance()) {
            return "You do not have enough money to buy this";
        }
        seller.addBalance(item.getPrice());
        buyer.setBalance(buyer.getBalance() - item.getPrice());
        deleteItem(item);
        return "Transaction occurs successfully";
    }

    /**
     * Setting up a timer 5 minutes to auto save everything instead of just write 1
     * time before
     * closing everything. Doing this will create regular backup in the case the
     * server crashes
     * or close unexpectedly.
     *
     *
     */
    public synchronized void setupAutoSave() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                writeToFile();
                System.out.println("Auto-save completed");
            }
        }, 10 * 60 * 1000, 10 * 60 * 1000); // Run every 10 minutes
    }

}
