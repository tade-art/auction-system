import java.rmi.registry.*;
import java.security.*;
import java.util.Scanner;

public class Client {
  private static KeyPair clientKeyPair;
  private static String token;

  public static void main(String[] args) {
    try {
      clientKeyPair = generateKeyPair();
      String name = "Auction";
      Registry registry = LocateRegistry.getRegistry("localhost");
      Auction server = (Auction) registry.lookup(name);
      Scanner scanner = new Scanner(System.in);

      while (true) {
        System.out.println("\nEnter command:");
        String command = scanner.nextLine();
        String[] commandArgs = command.split(" ");

        switch (commandArgs[0]) {

          // CASE FOR REGISTER COMMAND
          case "register":
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
              System.out
                  .println(userID != -1 ? "Registered successfully -> User ID: " + userID : "Registration failed.");
            }

            else
              System.out.println("User already exists.");
            break;

          // CASE FOR NEW COMMAND
          case "new":
            System.out.println("Creating new auction...");
            if (token != null) {
              AuctionSaleItem item = generateAuctionSaleItem(commandArgs[2], commandArgs[3],
                  Integer.parseInt(commandArgs[4]));
              int auctionID = server.newAuction(Integer.parseInt(commandArgs[1]), item, token);
              System.out
                  .println(auctionID != -1 ? "New auction created with ID: " + auctionID : "Failed to create auction.");
            } else
              System.out.println("Error with Token.");
            break;

          // CASE FOR CLOSE COMMAND
          case "close":
            if (token != null) {
              AuctionResult result = server.closeAuction(Integer.parseInt(commandArgs[1]),
                  Integer.parseInt(commandArgs[2]), token);
              if (result != null)
                System.out
                    .println("Auction closed. Winner: " + result.winningEmail + " with bid: " + result.winningPrice);
              else
                System.out.println("Failed to close auction.");
            }
            break;

          // CASE FOR BID COMMAND
          case "bid":
            if (token != null) {
              boolean bidSuccess = server.bid(Integer.parseInt(commandArgs[1]), Integer.parseInt(commandArgs[2]),
                  Integer.parseInt(commandArgs[3]), token);
              System.out.println(bidSuccess ? "Bid placed successfully." : "Failed to place bid.");
            }
            break;

          // CASE FOR LIST COMMAND
          case "list":
            if (token != null) {
              AuctionItem[] items = server.listItems(Integer.parseInt(commandArgs[1]), token);
              System.out.println("Auction Items:");
              for (AuctionItem auctionItem : items)
                System.out.println("ID: " + auctionItem.itemID + ", Name: " + auctionItem.name + ", Highest Bid: "
                    + auctionItem.highestBid);
            }
            break;

          // CASE FOR EXIT COMMAND
          case "exit":
            System.out.println("Exiting...");
            scanner.close();
            return;

          // DEFAULT CASE
          default:
            System.out
                .println("Unknown command. Available commands: new, close, bid, list, register, exit.");
            break;
        }
      }
    } catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
    }
  }

  // -----------------
  // HELPER FUNCTIONS
  // -----------------

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