import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server implements Auction {
    private ArrayList<AuctionItem> items = new ArrayList<AuctionItem>();
    private HashMap<Integer, String> userIDList = new HashMap<>();

    public Server() throws Exception {
        // KeyManager.generateAndStoreKey();
        // items.add(generateItem(1, "Item 1", "Description 1", 100));
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

    // -----------------------------------------
    // ----------NEW FUNCTIONS GO HERE----------
    // ----------------------------------------

    @Override
    public boolean bid(int userID, int itemID, int price, String token) throws RemoteException {
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

    @Override
    public ChallengeInfo challenge(int userID, String clientChallenge) throws RemoteException {
        throw new UnsupportedOperationException(" method 'challenge'");
    }

    @Override
    public TokenInfo authenticate(int userID, byte[] signature) throws RemoteException {
        throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
    }

    @Override
    public int register(String email, PublicKey pkey) throws RemoteException {
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
    public AuctionItem getSpec(int userID, int itemID, String token) throws RemoteException {
        if (items.get(itemID) != null)
            return items.get(itemID);
        return null;
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item, String token) throws RemoteException {
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
    public AuctionItem[] listItems(int userID, String token) throws RemoteException {
        try {
            return items.toArray(new AuctionItem[0]);
        } catch (Exception e) {
            System.err.println("Error in listItems:");
            e.printStackTrace();
        }
        return new AuctionItem[0];
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID, String token) throws RemoteException {
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
}