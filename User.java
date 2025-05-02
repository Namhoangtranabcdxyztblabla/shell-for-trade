import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class User implements UserInterface, Serializable {
    private String name;
    private String email;
    private String password;
    private double balance;
    private boolean onlineStatus;

    public User(String name, String email, String password, double balance) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.balance = balance;
    }
// the delete account should be in the database since we need to delete the account in the database
//    public void deleteAccount() {
//        name = null;
//        password = null;
//        email = null;
//        balance = 0;
//    }

    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public void setName(String name) throws InvalidAccountOperationException {
        // make sure the username has at least 1 character and does not start with a space
        if (name == null || name.startsWith(" ") || name.isEmpty() || name.trim().isEmpty()) {
            throw new InvalidAccountOperationException(
                    "Invalid username. Please make sure your username have at least 1 character and does not start with" +
                            " a space.");
        }
        this.name = name;
    }

    public void setEmail(String email) throws InvalidAccountOperationException {
        // Regular expression to match valid email formats
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        // Compile the regex
        Pattern p = Pattern.compile(emailRegex);

        //check the invalid email
        if (!(email != null && p.matcher(email).matches())) {
            throw new InvalidAccountOperationException("Invalid email! Please enter an valid email.");
        }
        this.email = email;
    }

    public void setPassword(String password) throws InvalidAccountOperationException {
        // make sure the password has at least 8 characters, and at least one uppercase
        // letter, one lowercase letter and one digit number， make sure the password does not start with a space
        if (password == null || password.contains(" ") || password.startsWith(" ") || password.length() < 8 ||
                !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") ||
                !password.matches(".*[0-9].*") || password.trim().isEmpty()) {
            throw new InvalidAccountOperationException("Invalid password. Password must be at least 8 characters and " +
                    "include at least one uppercase letter, one lowercase letter, and one digit. Password cannot start " +
                    "with a space.");
        }
        this.password = password;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void addBalance(double balance) {
        this.balance += balance;
    }

    // public ArrayList<Item> listItems() {
    //     ArrayList<Item> marketInventory = Item.getAllItems();
    //     ArrayList<Item> myItems = new ArrayList<>();
    //     for(Item item : marketInventory) {
    //         if(item.getOwner().equals(this)) {
    //             myItems.add(item);
    //         }
    //     }
    //     return myItems;
    // }

    // public ArrayList<Item> searchItems(String choice) {
    //     ArrayList<Item> marketInventory = Item.getAllItems();
    //     ArrayList<Item> searchList = new ArrayList<>();
    //     for(Item item : marketInventory) {
    //         if((item.getItemName().contains(choice) || choice.contains(item.getItemName())) && item.isForSale() && !item.getOwner().equals(this)) {
    //             searchList.add(item);
    //         }
    //     }

    //     return searchList;
    // }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;

        return  name.equals(user.getName()) && email.equals(user.getEmail()) && 
                password.equals(user.getPassword()) && balance == user.getBalance();
   
    }

    public String toString() {
        return name +
                ";" + password +
                ";" + email +
                ";" + balance;
    }

    // follows the format of name password email balance
    public String toFileString() {
        return name + "," + password + "," + email + "," + balance;
    }
}
