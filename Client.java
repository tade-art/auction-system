import java.rmi.registry.*;
import java.security.*;
import java.util.Scanner;

public class Client {
  private static KeyPair clientKeyPair;
  private static String token;

  public static void main(String[] args) {
    try {
      clientKeyPair = generateKeyPair();
      Registry registry = LocateRegistry.getRegistry("localhost");
      Auction server = (Auction) registry.lookup("Auction");
      Scanner scanner = new Scanner(System.in);

      while (true) {
        System.out.println("\nEnter command:");
        String command = scanner.nextLine();
        String[] commandArgs = command.split(" ");

        switch (commandArgs[0]) {
          case "register":
            handleRegisterCommand(server, commandArgs);
            break;
          case "new":
            handleNewCommand(server, commandArgs);
            break;
          case "close":
            handleCloseCommand(server, commandArgs);
            break;
          case "bid":
            handleBidCommand(server, commandArgs);
            break;
          case "list":
            handleListCommand(server, commandArgs);
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

  private static void handleRegisterCommand(Auction server, String[] commandArgs) throws Exception {
    int userID = server.register(commandArgs[1], clientKeyPair.getPublic());
    if (userID != -1) {
      ChallengeInfo challengeInfo = server.challenge(userID, Integer.toString(userID));
      Signature sig = Signature.getInstance("SHA256withRSA");
      sig.initSign(clientKeyPair.getPrivate());
      sig.update(challengeInfo.serverChallenge.getBytes());
      byte[] clientSignature = sig.sign();

      TokenInfo tokenInfo = server.authenticate(userID, clientSignature);

      if (tokenInfo != null)
        token = tokenInfo.token;
      else
        System.out.println("Authentication failed.");
      System.out.println(userID != -1 ? "Registered successfully -> User ID: " + userID : "Registration failed.");
    } else {
      System.out.println("User already exists.");
    }
  }

  private static void handleNewCommand(Auction server, String[] commandArgs) throws Exception {
    if (token != null) {
      AuctionSaleItem item = generateAuctionSaleItem(commandArgs[2], commandArgs[3], Integer.parseInt(commandArgs[4]));
      int auctionID = server.newAuction(Integer.parseInt(commandArgs[1]), item, token);
      System.out.println(auctionID != -1 ? "New auction created with ID: " + auctionID : "Failed to create auction.");
    } else {
      System.out.println("Error with Token.");
    }
  }

  private static void handleCloseCommand(Auction server, String[] commandArgs) throws Exception {
    if (token != null) {
      AuctionResult result = server.closeAuction(Integer.parseInt(commandArgs[1]), Integer.parseInt(commandArgs[2]),
          token);
      if (result != null)
        System.out.println("Auction closed. Winner: " + result.winningEmail + " with bid: " + result.winningPrice);
      else
        System.out.println("Failed to close auction.");
    }
  }

  private static void handleBidCommand(Auction server, String[] commandArgs) throws Exception {
    if (token != null) {
      boolean bidSuccess = server.bid(Integer.parseInt(commandArgs[1]), Integer.parseInt(commandArgs[2]),
          Integer.parseInt(commandArgs[3]), token);
      System.out.println(bidSuccess ? "Bid placed successfully." : "Failed to place bid.");
    }
  }

  private static void handleListCommand(Auction server, String[] commandArgs) throws Exception {
    if (token != null) {
      if (commandArgs.length != 2) {
        System.out.println("Invalid command. Usage: list <userID>");
        return;
      }
      AuctionItem[] items = server.listItems(Integer.parseInt(commandArgs[1]), token);
      System.out.println("Auction Items:");
      for (AuctionItem auctionItem : items)
        System.out.println("ID: " + auctionItem.itemID + ", Name: " + auctionItem.name + ", Highest Bid: "
            + auctionItem.highestBid + "\n");
    }
  }

  private static void printUsage() {
    System.out.println("Available commands:");
    System.out.println("register <email>");
    System.out.println("new <userID> <itemName> <itemDescription> <reservePrice>");
    System.out.println("close <userID> <auctionID>");
    System.out.println("bid <userID> <auctionID> <bidAmount>");
    System.out.println("list <userID>");
    System.out.println("exit");
  }

  private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    return keyGen.generateKeyPair();
  }

  public static AuctionSaleItem generateAuctionSaleItem(String name, String description, int reservePrice) {
    AuctionSaleItem toReturn = new AuctionSaleItem();
    toReturn.name = name;
    toReturn.description = description;
    toReturn.reservePrice = reservePrice;
    return toReturn;
  }
}