import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
  public static void main(String[] args) {
    try {
      String name = "Auction";
      Registry registry = LocateRegistry.getRegistry("localhost");
      Auction server = (Auction) registry.lookup(name);

      if (args.length == 0) {
        System.out.println("Please specify a command: new, close, bid, list, or register");
        return;
      }

      // switch (args[0]) {
      // case "new":
      // if (args.length < 4) {
      // System.out.println("Usage: new <userID> <itemName> <description>
      // <reservePrice>");
      // return;
      // }

      // AuctionSaleItem item = new AuctionSaleItem();
      // int userID = Integer.parseInt(args[1]);
      // item.name = args[2];
      // ;
      // item.description = args[3];
      // item.reservePrice = Integer.parseInt(args[4]);
      // int auctionID = server.newAuction(userID, item);
      // System.out.println("New auction created with ID: " + auctionID);
      // break;

      // case "close":
      // if (args.length < 3) {
      // System.out.println("Usage: close <userID> <itemID>");
      // return;
      // }

      // AuctionResult result = server.closeAuction(Integer.parseInt(args[1]),
      // Integer.parseInt(args[2]));
      // if (result != null)
      // System.out.println("Auction closed. Winner: " + result.winningEmail + " with
      // bid: " + result.winningPrice);
      // else
      // System.out.println("Failed to close auction.");
      // break;

      // case "bid":
      // if (args.length < 4) {
      // System.out.println("Usage: bid <userID> <itemID> <price>");
      // return;
      // }
      // boolean bidSuccess = server.bid(Integer.parseInt(args[1]),
      // Integer.parseInt(args[2]),
      // Integer.parseInt(args[3]));

      // System.out.println(bidSuccess ? "Bid placed successfully." : "Failed to place
      // bid.");
      // break;

      // case "list":
      // AuctionItem[] items = server.listItems();
      // System.out.println("Auction Items:");
      // for (AuctionItem auctionItem : items)
      // System.out.println("ID: " + auctionItem.itemID + ", Name: " +
      // auctionItem.name + ", Highest Bid: "
      // + auctionItem.highestBid);
      // break;

      // case "register":
      // if (args.length < 2) {
      // System.out.println("Usage: register <email>");
      // return;
      // }

      // int registeredID = server.register(args[1]);
      // System.out.println(
      // registeredID != -1 ? "Registered successfully -> User ID: " + registeredID :
      // "Registration failed.");
      // break;

      // default:
      // System.out.println("Unknown command. Available commands: new, close, bid,
      // list, register.");
      // break;
      // }
    } catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
    }
  }
}