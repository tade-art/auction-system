import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Server implements Auction {
    private ArrayList<AuctionItem> items = new ArrayList<AuctionItem>();
    private HashMap<Integer, String> userIDList = new HashMap<>();

    public Server() throws Exception {
        // KeyManager.generateAndStoreKey();
        items.add(generateItem(1, "Item 1", "Description 1", 100));
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

    public AuctionItem generateItem(int itemID, String name, String description, int highestBid) {
        AuctionItem item = new AuctionItem();
        item.itemID = itemID;
        item.name = name;
        item.description = description;
        item.highestBid = highestBid;
        return item;
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        if (items.get(itemID) != null)
            return items.get(itemID);
        return null;
    }

    // -----------------------------------------------
    // VVVVV ----- NEW FUNCTIONS GO HERE ----- VVVVVV
    // -----------------------------------------------

    @Override
    public int register(String email) throws RemoteException {
        try {
            if (userIDList.containsValue(email))
                return -1;

            else {
                int userID = 0;
                while (userID == 0 || userIDList.containsKey(userID))
                    userID = (int) (Math.random() * 10000);

                userIDList.put(userID, email);
                return userID;
            }
        }

        catch (Exception e) {
            System.err.println("Error with registering:");
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        try {
        } catch (Exception e) {
            System.err.println("Error with newAuction:");
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        try {
        } catch (Exception e) {
            System.err.println("Error in listItems:");
            e.printStackTrace();
        }
        return new AuctionItem[0];
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        try {
        } catch (Exception e) {
            System.err.println("Error in closeAuction:");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        try {
        } catch (Exception e) {
            System.err.println("Error in bid:");
            e.printStackTrace();
        }
        return false;
    }
}