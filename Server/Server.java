import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/*
 * Server class that contains server information as well as function implementations
 */
public class Server implements IAuction {
    private AuctionItem[] items;

    /*
     * Constructor for the Server class
     */
    public Server() {
        super();

        // Initialize the hardcoded list of AuctionItems
        items = new AuctionItem[3];
        items[0] = new AuctionItem(1, "Apple", "It's food", 1);
        items[1] = new AuctionItem(2, "T-Shirt", "It's clothing", 50);
        items[2] = new AuctionItem(3, "Car", "It's transport", 30000);
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "Auction Server";
            IAuction stub = (IAuction) UnicastRemoteObject.exportObject(s, 8080);
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
