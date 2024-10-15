/*
 * File that contains information about the Item being passed around from client to client
 */
public class AuctionItem implements java.io.Serializable {
    int itemID;
    String name;
    String description;
    int highestBid;
}