import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
  public static void main(String[] args) {
    try {
      Registry registry = LocateRegistry.getRegistry("localhost");
      Auction server = (Auction) registry.lookup("FrontEnd");
      Scanner scanner = new Scanner(System.in);

      while (true) {
        System.out.println("\nEnter command: ");
        String command = scanner.nextLine();
        String[] commandArgs = command.split(" ");

        switch (commandArgs[0]) {
          case "register":
            handleRegister(commandArgs, server);
            break;
          case "new":
            handleNewAuction(commandArgs, server);
            break;
          case "close":
            handleCloseAuction(commandArgs, server);
            break;
          case "bid":
            handleBid(commandArgs, server);
            break;
          case "list":
            handleListItems(server);
            break;
          case "usage":
            printUsage();
            break;
          case "exit":
            System.out.println("Exiting...");
            scanner.close();
            return;
          default:
            System.out.println("Unknown command. Check usage for commands.");
            break;
        }
      }
    }

    catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
    }
  }

  private static void handleNewAuction(String[] args, Auction server) throws Exception {
    AuctionSaleItem item = new AuctionSaleItem();
    int userID = Integer.parseInt(args[1]);
    item.name = args[2];
    item.description = args[3];
    item.reservePrice = Integer.parseInt(args[4]);
    int auctionID = server.newAuction(userID, item);
    System.out.println("New auction created with ID: " + auctionID);
  }

  private static void handleCloseAuction(String[] args, Auction server) throws Exception {
    AuctionResult result = server.closeAuction(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    if (result != null)
      System.out.println("Auction closed. Winner: " + result.winningEmail + " with bid: " + result.winningPrice);
    else
      System.out.println("Failed to close auction.");
  }

  private static void handleBid(String[] args, Auction server) throws Exception {
    boolean bidSuccess = server.bid(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    System.out.println(bidSuccess ? "Bid placed successfully." : "Failed to place bid.");
  }

  private static void handleListItems(Auction server) throws Exception {
    AuctionItem[] items = server.listItems();
    System.out.println("Auction Items:");

    for (AuctionItem auctionItem : items)
      System.out.println(
          "ID: " + auctionItem.itemID + ", Name: " + auctionItem.name + ", Highest Bid: " + auctionItem.highestBid);
  }

  private static void handleRegister(String[] args, Auction server) throws Exception {
    int registeredID = server.register(args[1]);
    System.out
        .println(registeredID != -1 ? "Registered successfully -> User ID: " + registeredID : "Registration failed.");
  }

  private static void printUsage() {
    System.out.println("Usage:");
    System.out.println("  new <userID> <itemName> <description> <reservePrice> - Create a new auction");
    System.out.println("  close <userID> <itemID> - Close an auction");
    System.out.println("  bid <userID> <itemID> <price> - Place a bid on an auction");
    System.out.println("  list - List all auction items");
    System.out.println("  register <email> - Register a new user");
    System.out.println("  exit - Exit the program");
  }

  public static AuctionSaleItem generateAuctionSaleItem(String name, String description, int reservePrice) {
    AuctionSaleItem toReturn = new AuctionSaleItem();
    toReturn.name = name;
    toReturn.description = description;
    toReturn.reservePrice = reservePrice;
    return toReturn;
  }
}