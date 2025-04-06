public interface ItemInterface {

    public User getOwner();
    public String getItemName();
    public double getPrice();
    public boolean isForSale();
    public void setOwner(User owner);
    public void setItemName(String name);
    public void setPrice(double price);
    public void setForSale(boolean forSale);

}
