import java.rmi.registry.*;
import java.security.*;
import java.util.Base64;

public class Client {
  private static KeyPair clientKeyPair;
  private static String token;

  public static void main(String[] args) {
    try {
      clientKeyPair = generateKeyPair();
      String name = "Auction";
      Registry registry = LocateRegistry.getRegistry("localhost");
      Auction server = (Auction) registry.lookup(name);

      switch (args[0]) {

        // CASE FOR REGISTER COMMAND
        case "register":
          int userID = server.register(args[1], clientKeyPair.getPublic());
          System.out.println(userID != -1 ? "Registered successfully -> User ID: " + userID : "Registration failed.");
          break;

        //
        //
        //
        //
        //
        // ERROR HERE - SERVER AND CLIENT SIGS AREN@T MATCHING AND CANNOT VERIFY ITSELF
        case "authenticate":
          try {
            ChallengeInfo challengeInfo = server.challenge(Integer.parseInt(args[1]), "clientChallenge");
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(clientKeyPair.getPrivate());
            signature.update(challengeInfo.serverChallenge.getBytes());
            byte[] clientSignature = signature.sign();
            TokenInfo tokenInfo = server.authenticate(Integer.parseInt(args[1]), clientSignature);

            if (tokenInfo != null)
              token = tokenInfo.token;
            else
              System.out.println("Authentication failed.");
          }

          catch (Exception e) {
            System.err.println("Error during authentication:");
            e.printStackTrace();
          }
          break;
        // ERROR ABOVE - NOT WORKING
        //
        //
        //
        //
        //

        // CASE FOR NEW COMMAND
        case "new":
          if (token != null) {
            AuctionSaleItem item = generateAuctionSaleItem(args[2], args[3], Integer.parseInt(args[4]));
            int auctionID = server.newAuction(Integer.parseInt(args[1]), item, token);
            System.out
                .println(auctionID != -1 ? "New auction created with ID: " + auctionID : "Failed to create auction.");
          }
          break;

        // CASE FOR CLOSE COMMAND
        case "close":
          if (token != null) {
            AuctionResult result = server.closeAuction(Integer.parseInt(args[1]), Integer.parseInt(args[2]), token);
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
            boolean bidSuccess = server.bid(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                Integer.parseInt(args[3]), token);
            System.out.println(bidSuccess ? "Bid placed successfully." : "Failed to place bid.");
          }
          break;

        // CASE FOR LIST COMMAND
        case "list":
          if (token != null) {
            AuctionItem[] items = server.listItems(Integer.parseInt(args[1]), token);
            System.out.println("Auction Items:");
            for (AuctionItem auctionItem : items)
              System.out.println("ID: " + auctionItem.itemID + ", Name: " + auctionItem.name + ", Highest Bid: "
                  + auctionItem.highestBid);
          }
          break;

        // DEFAULT CASE
        default:
          System.out.println("Unknown command. Available commands: new, close, bid, list, register, authenticate.");
          break;
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