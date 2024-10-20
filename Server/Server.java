import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements Auction {
    private AuctionItem[] items;
    private SecretKey aesKey;

    public Server() throws Exception {
        KeyManager.generateAndStoreKey();
        aesKey = KeyManager.loadKey();

        items = new AuctionItem[3];
        items[0] = new AuctionItem(1, "Item 1", "Description for item 1", 100);
        items[1] = new AuctionItem(2, "Item 2", "Description for item 2", 200);
        items[2] = new AuctionItem(3, "Item 3", "Description for item 3", 300);
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "Auction";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.createRegistry(8080);
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
}