import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Server implements Auction {
    private ArrayList<AuctionItem> items = new ArrayList<AuctionItem>();
    private SecretKey aesKey;

    public Server() throws Exception {
        KeyManager.generateAndStoreKey();
        aesKey = KeyManager.loadKey();

        items.add(generateItem(1, "Item 1", "Description 1", 100));
        items.add(generateItem(2, "Item 2", "Description 2", 200));
        items.add(generateItem(3, "Item 3", "Description 3", 300));
        System.out.println(items.get(1).name);
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "Auction";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Something went wrong here:");
            e.printStackTrace();
        }
    }

    @Override
    public SealedObject getSpec(int itemID) throws RemoteException {
        try {
            for (AuctionItem item : items) {
                if (item.itemID == itemID) {
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                    return new SealedObject(item, cipher);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public AuctionItem generateItem(int itemID, String name, String description, int highestBid) {
        AuctionItem item = new AuctionItem();
        item.itemID = itemID;
        item.name = name;
        item.description = description;
        item.highestBid = highestBid;
        return item;
    }
}