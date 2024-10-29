import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
  public static void main(String[] args) {
    int n = Integer.parseInt(args[0]);

    try {
      String name = "Auction";
      Registry registry = LocateRegistry.getRegistry("localhost");
      Auction server = (Auction) registry.lookup(name);

      AuctionItem toReturn = server.getSpec(n);
      System.out.println("object returned: " + toReturn.itemID + "\nObject Name:" + toReturn.name
          + "\nObject Description:" + toReturn.description + "\nObject highest bid:" + toReturn.highestBid);

    } catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
    }
  }
}
