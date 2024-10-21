import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SealedObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
  public static void main(String[] args) {
    int n = Integer.parseInt(args[0]);

    try {
      SecretKey aesKey = KeyManager.loadKey();

      String name = "Auction";
      Registry registry = LocateRegistry.getRegistry("localhost");
      Auction server = (Auction) registry.lookup(name);
      SealedObject sealedObject = server.getSpec(n);

      // Decrypt the object
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, aesKey);
      AuctionItem decryptedItem = (AuctionItem) sealedObject.getObject(cipher);
      System.out.println("object returned: " + decryptedItem.itemID +
          "\nObject Name: " + decryptedItem.name +
          "\nObject Description: " + decryptedItem.description +
          "\nObject highest bid: " + decryptedItem.highestBid);
    } catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
    }
  }
}
