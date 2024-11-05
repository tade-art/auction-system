import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

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

    public AuctionResult generateAuctionResult(String email, int winningBid) {
        AuctionResult toReturn = new AuctionResult();
        toReturn.winningEmail = email;
        toReturn.winningPrice = winningBid;
        return toReturn;
    }

    public AuctionSaleItem generateAuctionSaleItem(String name, String description, int reservePrice) {
        AuctionSaleItem toReturn = new AuctionSaleItem();
        toReturn.name = name;
        toReturn.description = description;
        toReturn.reservePrice = reservePrice;
        return toReturn;
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
            if (!userIDList.containsKey(userID) || item == null)
                return -1;

            int auctionID = 0;

            boolean exists = true;
            while (auctionID == 0 || exists) {
                auctionID = (int) (Math.random() * 10000);
                exists = false;
                for (AuctionItem i : items) {
                    if (i.itemID == auctionID) {
                        exists = true;
                        break;
                    }
                }
            }

            AuctionItem auctionItem = generateItem(auctionID, item.name, item.description, item.reservePrice);
            items.add(auctionItem);
            return auctionID;
        }

        catch (Exception e) {
            System.err.println("Error with newAuction:");
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        try {
            return (AuctionItem[]) items.toArray();
        }

        catch (Exception e) {
            System.err.println("Error in listItems:");
            e.printStackTrace();
        }
        return new AuctionItem[0];
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        try {
            if (!userIDList.containsKey(userID))
                return null;

            AuctionItem item = null;
            for (AuctionItem i : items)
                if (i.itemID == itemID) {
                    item = i;
                    break;
                }

            items.remove(item);
            return generateAuctionResult(userIDList.get(userID), item.highestBid);
        }

        catch (Exception e) {
            System.err.println("Error in closeAuction:");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        try {
            if (!userIDList.containsKey(userID))
                return false;

            AuctionItem item = null;
            for (AuctionItem i : items)
                if (i.itemID == itemID) {
                    item = i;
                    break;
                }

            if (item == null || price <= item.highestBid || price < 0)
                return false;

            item.highestBid = price;
            return true;
        }

        catch (Exception e) {
            System.err.println("Error in bid:");
            e.printStackTrace();
        }
        return false;
    }
}