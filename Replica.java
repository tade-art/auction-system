import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Replica implements IReplica {
    private HashMap<Integer, List<AuctionItem>> items = new HashMap<>();
    private HashMap<Integer, String> userIDList = new HashMap<>();
    private HashMap<Integer, Integer> currHighestBidder = new HashMap<>();
    private static ArrayList<Integer> listOfReplicas = new ArrayList<>();
    private int replicaID;

    public Replica() throws Exception {
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                Replica replica = new Replica();
                replica.replicaID = Integer.parseInt(args[0]);
                listOfReplicas.add(replica.replicaID);

                String name = "Replica " + replica.replicaID;
                Registry registry = LocateRegistry.getRegistry("localhost");
                IReplica stub = (IReplica) UnicastRemoteObject.exportObject(replica, 0);
                registry.rebind(name, stub);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Please provide a replica ID as an argument.");
        }
    }

    // ______________________________
    // Helper methods
    // _____________________________

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
        synchroniseState();
        return userID;
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        AuctionItem item = getSpec(itemID);
        if (item != null && price > item.highestBid) {
            item.highestBid = price;
            currHighestBidder.put(itemID, userID);
            synchroniseState();
            return true;
        }
        synchroniseState();
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
        synchroniseState();
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
            AuctionResult result = generateAuctionResult(userIDList.get(currHighestBidder.get(itemID)),
                    item.highestBid);
            synchroniseState();
            return result;
        }
        synchroniseState();
        return null;
    }

    // ______________________________
    // New Ireplica methods
    // _____________________________

    @Override
    public int getReplicaID() throws RemoteException {
        return replicaID;
    }

    @Override
    public void synchroniseState() throws RemoteException {
        for (int replicaID : listOfReplicas) {
            if (replicaID == this.replicaID)
                continue;
            try {
                IReplica replica = (IReplica) LocateRegistry.getRegistry("localhost").lookup("Replica " + replicaID);
                replica.clone(items, userIDList, currHighestBidder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        System.out.println("avoid plz and thanks");
        return 0;
    }

    @Override
    public void UpdateListInPrimaryReplica(ArrayList<Integer> replicas) throws RemoteException {
        listOfReplicas.clear();
        listOfReplicas.addAll(replicas);
    }

    @Override
    public void clone(HashMap<Integer, List<AuctionItem>> items, HashMap<Integer, String> userIDList,
            HashMap<Integer, Integer> currHighestBidder) throws RemoteException {

        // Deep copy `items`
        this.items = new HashMap<>();
        for (Map.Entry<Integer, List<AuctionItem>> entry : items.entrySet()) {
            List<AuctionItem> clonedList = new ArrayList<>();
            for (AuctionItem item : entry.getValue()) {
                AuctionItem clonedItem = generateItem(item.itemID, item.name, item.description, item.highestBid);
                clonedList.add(clonedItem);
            }
            this.items.put(entry.getKey(), clonedList);
        }

        // Shallow copy `userIDList` and `currHighestBidder`
        this.userIDList = new HashMap<>(userIDList);
        this.currHighestBidder = new HashMap<>(currHighestBidder);
    }

}
