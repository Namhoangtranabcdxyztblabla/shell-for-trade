//import java.util.ArrayList;


public interface UserInterface {
    

//    public void deleteAccount();
    public String getName();
    public String getEmail();
    public String getPassword();
    public double getBalance();
    public void setName(String name) throws InvalidAccountOperationException;
    public void setEmail(String Email) throws InvalidAccountOperationException;
    public void setPassword(String password) throws InvalidAccountOperationException;
    public void setBalance(double balance);
    //public ArrayList<Item> listItems();
    public String toString();

}
