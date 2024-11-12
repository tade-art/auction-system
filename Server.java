import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements Auction {
    private HashMap<Integer, List<AuctionItem>> items = new HashMap<>();
    private HashMap<Integer, String> userIDList = new HashMap<>();
    private HashMap<Integer, PublicKey> userKeys = new HashMap<>();
    private ConcurrentHashMap<Integer, TokenInfo> tokenMap = new ConcurrentHashMap<>();
    private KeyPair serverKeyPair;

    public Server() throws Exception {
        this.serverKeyPair = generateKeyPair();
        savePublicKey(this.serverKeyPair.getPublic(), "keys/server_public.key");
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

    public ChallengeInfo generateChallengeInfo(byte[] signedChallenge, String serverChallenge) {
        ChallengeInfo toReturn = new ChallengeInfo();
        toReturn.response = signedChallenge;
        toReturn.serverChallenge = serverChallenge;
        return toReturn;
    }

    public TokenInfo generateTokenInfo(String token, long expiryTime) {
        TokenInfo toReturn = new TokenInfo();
        toReturn.token = token;
        toReturn.expiryTime = expiryTime;
        return toReturn;
    }

    // -----------------------------------------
    // ----------NEW FUNCTIONS GO HERE----------
    // -----------------------------------------

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    private void savePublicKey(PublicKey publicKey, String filePath) throws IOException {
        Files.createDirectories(Paths.get("keys"));
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(publicKey.getEncoded());
        }
    }

    @Override
    public ChallengeInfo challenge(int userID, String clientChallenge) throws RemoteException {
        if (!userIDList.containsKey(userID)) {
            System.out.println("User not registered: " + userID);
            throw new RemoteException("User not registered.");
        }

        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(serverKeyPair.getPrivate());
            signature.update(clientChallenge.getBytes());

            byte[] signedChallenge = signature.sign();
            String serverChallenge = Integer.toString(userID);

            return generateChallengeInfo(signedChallenge, serverChallenge);
        }

        catch (Exception e) {
            System.err.println("Error in challenge generation: " + e.getMessage());
            throw new RemoteException("Challenge generation failed.", e);
        }
    }

    @Override
    public TokenInfo authenticate(int userID, byte[] clientSignature) throws RemoteException {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            String serverChallenge = Integer.toString(userID);

            sig.initVerify(userKeys.get(userID));
            sig.update(serverChallenge.getBytes());

            if (sig.verify(clientSignature)) {
                String token = Base64.getEncoder().encodeToString(("token" + userID).getBytes());
                long expiryTime = System.currentTimeMillis() + 10000;
                TokenInfo tokenInfo = generateTokenInfo(token, expiryTime);
                tokenMap.put(userID, tokenInfo);
                return tokenInfo;
            }

            else
                System.out.println("Signature verification failed for userID: " + userID);

        } catch (Exception e) {
            System.err.println("Error in authentication: " + e.getMessage());
            throw new RemoteException("Authentication failed.", e);
        }
        return null;
    }

    @Override
    public int register(String email, PublicKey pkey) throws RemoteException {
        try {
            if (userIDList.containsValue(email))
                return -1;

            int userID = (int) (Math.random() * 10000);
            while (userIDList.containsKey(userID))
                userID = (int) (Math.random() * 10000);

            userIDList.put(userID, email);
            userKeys.put(userID, pkey);
            return userID;
        }

        catch (Exception e) {
            System.err.println("Error with registering:");
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean bid(int userID, int itemID, int price, String token) throws RemoteException {
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
            return true;
        }

        catch (Exception e) {
            System.err.println("Error in bid:");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public AuctionItem getSpec(int userID, int itemID, String token) throws RemoteException {
        List<AuctionItem> userItems = items.get(userID);
        if (userItems != null) {
            for (AuctionItem item : userItems) {
                if (item.itemID == itemID) {
                    return item;
                }
            }
        }
        return null;
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item, String token) throws RemoteException {
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
    public AuctionResult closeAuction(int userID, int itemID, String token) throws RemoteException {
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
                return generateAuctionResult(userIDList.get(userID), item.highestBid);
            }
        }

        catch (Exception e) {
            System.err.println("Error in closeAuction:");
            e.printStackTrace();
        }
        return null;
    }
}