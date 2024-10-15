/*
 * File that contains information about the Item being passed around from client to client
 */
public class AuctionItem implements java.io.Serializable {
    int itemID;
    String name;
    String description;
    int highestBid;

    /*
     * Constructor for the AuctionItem class
     */
    public AuctionItem(int itemID, String name, String description, int highestBid) {
        this.itemID = itemID;
        this.name = name;
        this.description = description;
        this.highestBid = highestBid;
    }

    /*
     * Function that returns the itemID
     */
    public String toString() {
        return "Item ID: " + itemID + " Name: " + name + " Description: " + description + " Highest Bid: " + highestBid;
    }
}