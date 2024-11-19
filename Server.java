import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;

public class Server implements Auction {
    private HashMap<Integer, List<AuctionItem>> items = new HashMap<>();
    private HashMap<Integer, String> userIDList = new HashMap<>();
    private HashMap<Integer, Integer> currHighestBidder = new HashMap<>();

    public Server() throws Exception {
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

    // ----------------
    // HELPER FUNCTIONS
    // ----------------

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

    // -----------------------------------------
    // ----------NEW FUNCTIONS GO HERE----------
    // -----------------------------------------

    @Override
    public int register(String email) throws RemoteException {
        try {
            if (userIDList.containsValue(email))
                return -1;

            int userID = (int) (Math.random() * 10000);
            while (userIDList.containsKey(userID))
                userID = (int) (Math.random() * 10000);

            userIDList.put(userID, email);
            return userID;
        }

        catch (Exception e) {
            System.err.println("Error with registering:");
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        try {
            if (!userIDList.containsKey(userID))
                return false;

            AuctionItem item = null;
            for (List<AuctionItem> itemList : items.values()) {
                for (AuctionItem i : itemList) {
                    if (i.itemID == itemID) {
                        item = i;
                        break;
                    }
                }
                if (item != null)
                    break;
            }

            if (item == null || price <= item.highestBid || price < 0)
                return false;

            item.highestBid = price;
            currHighestBidder.put(itemID, userID);
            return true;
        }

        catch (Exception e) {
            System.err.println("Error in bid:");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        for (List<AuctionItem> userItems : items.values()) {
            for (AuctionItem item : userItems) {
                if (item.itemID == itemID) {
                    return item;
                }
            }
        }
        return null;
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        try {
            int auctionID = 0;
            boolean exists = true;
            while (auctionID == 0 || exists) {
                auctionID = (int) (Math.random() * 10000);
                final int finalAuctionID = auctionID;
                exists = items.values().stream().anyMatch(
                        list -> list.stream().anyMatch(auctionItem -> auctionItem.itemID == finalAuctionID));
            }

            AuctionItem auctionItem = generateItem(auctionID, item.name, item.description, item.reservePrice);
            items.computeIfAbsent(userID, k -> new ArrayList<>()).add(auctionItem);
            currHighestBidder.put(auctionID, userID);
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
            List<AuctionItem> allItems = new ArrayList<>();
            items.values().forEach(allItems::addAll);
            return allItems.toArray(new AuctionItem[0]);
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

            List<AuctionItem> userItems = items.get(userID);
            if (userItems == null)
                return null;

            AuctionItem item = null;
            for (AuctionItem i : userItems) {
                if (i.itemID == itemID) {
                    item = i;
                    break;
                }
            }

            if (item != null) {
                userItems.remove(item);
                return generateAuctionResult(userIDList.get(currHighestBidder.get(itemID)), item.highestBid);
            }
        } catch (Exception e) {
            System.err.println("Error in closeAuction:");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        throw new UnsupportedOperationException("Unimplemented method 'getPrimaryReplicaID'");
    }
}