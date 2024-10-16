import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/*
 * Server class that contains server information as well as function implementations
 */
public class Server implements Auction {
    private AuctionItem[] items;

    /*
     * Constructor for the Server class
     */
    public Server() {
        super();
        items = new AuctionItem[3];
        items[0] = new AuctionItem(1, "Item 1", "Description for item 1", 100);
        items[1] = new AuctionItem(2, "Item 2", "Description for item 2", 200);
        items[2] = new AuctionItem(3, "Item 3", "Description for item 3", 300);
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "Auction";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 8080);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Something went wrong here:");
            e.printStackTrace();
        }
    }

    /*
     * Implementation of the getSpec function - Returns obj which matches the itemID
     */
    public AuctionItem getSpec(int itemID) throws RemoteException {
        for (AuctionItem item : items)
            if (item.itemID == itemID)
                return item;
        return null;
    }
}