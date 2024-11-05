import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
  public static void main(String[] args) {

    try {
      String name = "Auction";
      Registry registry = LocateRegistry.getRegistry("localhost");
      Auction server = (Auction) registry.lookup(name);

      // FIGURE OUT A WAY TO GENERATE NEW AUCTION, KEEP IT CLEAN AND TIDY
      switch (args[0]) {
        case "new":
          // server.newAuction(args[1], server.generateAuctionSaleItem((args[1]), args[2],
          // Integer.parseInt(args[3])));
        case "close":
          server.closeAuction(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        case "bid":
          server.bid(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        case "list":
          server.listItems();
        case "register":
          server.register(args[1]);
      }

      // AuctionItem toReturn = server.getSpec(n);
    }

    catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
    }
  }
}
