import java.io.Serializable;

public class Item implements ItemInterface, Serializable {

    private User owner;
    private String itemName;
    private double price;
    private String description;
    private boolean forSale;


    public Item(User owner, String itemName, double price,String description, boolean forSale) {
        this.owner = owner;
        this.itemName = itemName;
        this.price = price;
        this.forSale = forSale;
        this.description = description;
    }
    
    public User getOwner() {
        return owner;
    }

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public String toFileString() {
        return owner.getName() + "," + itemName + "," + price + "," + description + "," + forSale;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
