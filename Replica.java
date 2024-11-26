import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Replica implements Auction {
    private HashMap<Integer, List<AuctionItem>> items = new HashMap<>();
    private HashMap<Integer, String> userIDList = new HashMap<>();
    private HashMap<Integer, Integer> currHighestBidder = new HashMap<>();
    private int replicaID;

    public Replica() throws Exception {
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                int id = Integer.parseInt(args[0]);
                Replica replica = new Replica();
                replica.replicaID = id;
                System.out.println("Replica started with ID: " + id);

                // Connect to FrontEnd and add this Replica
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                Auction frontEnd = (Auction) registry.lookup("FrontEnd");
                frontEnd.addReplica(replica);
                System.out.println("Replica with ID: " + id + " added to FrontEnd.");

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Please provide a replica ID as an argument.");
        }
    }

    public void updateStateFromPrimary(Replica primaryReplica) throws RemoteException {
        this.items = new HashMap<>(primaryReplica.items);
        this.userIDList = new HashMap<>(primaryReplica.userIDList);
        this.currHighestBidder = new HashMap<>(primaryReplica.currHighestBidder);
        System.out.println("Replica state updated from Primary Replica.");
    }

    protected AuctionItem generateItem(int itemID, String name, String description, int highestBid) {
        AuctionItem item = new AuctionItem();
        item.itemID = itemID;
        item.name = name;
        item.description = description;
        item.highestBid = highestBid;
        return item;
    }

    protected AuctionResult generateAuctionResult(String email, int winningBid) {
        AuctionResult toReturn = new AuctionResult();
        toReturn.winningEmail = email;
        toReturn.winningPrice = winningBid;
        return toReturn;
    }

    protected AuctionSaleItem generateAuctionSaleItem(String name, String description, int reservePrice) {
        AuctionSaleItem toReturn = new AuctionSaleItem();
        toReturn.name = name;
        toReturn.description = description;
        toReturn.reservePrice = reservePrice;
        return toReturn;
    }

    @Override
    public int register(String email) throws RemoteException {
        if (userIDList.containsValue(email)) {
            return -1;
        }

        int userID = (int) (Math.random() * 10000);
        while (userIDList.containsKey(userID)) {
            userID = (int) (Math.random() * 10000);
        }

        userIDList.put(userID, email);
        return userID;
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        AuctionItem item = getSpec(itemID);
        if (item != null && price > item.highestBid) {
            item.highestBid = price;
            currHighestBidder.put(itemID, userID);
            return true;
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
        int auctionID = (int) (Math.random() * 10000);
        AuctionItem auctionItem = generateItem(auctionID, item.name, item.description, item.reservePrice);
        items.computeIfAbsent(userID, k -> new ArrayList<>()).add(auctionItem);
        currHighestBidder.put(auctionID, userID);
        return auctionID;
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        List<AuctionItem> allItems = new ArrayList<>();
        items.values().forEach(allItems::addAll);
        return allItems.toArray(new AuctionItem[0]);
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        AuctionItem item = getSpec(itemID);
        if (item != null) {
            items.get(userID).remove(item);
            return generateAuctionResult(userIDList.get(currHighestBidder.get(itemID)), item.highestBid);
        }
        return null;
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        return replicaID;
    }
}
