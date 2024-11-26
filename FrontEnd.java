import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class FrontEnd implements Auction {
    protected static ArrayList<Replica> replicas = new ArrayList<>();
    protected static int amountOfReplicas = 3;
    protected static Replica primaryReplica;

    public static void main(String[] args) throws Exception {
        try {
            FrontEnd frontEnd = new FrontEnd();
            String name = "FrontEnd";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(frontEnd, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);

            // Initialize replicas
            for (int i = 0; i < amountOfReplicas; i++) {
                Replica replica = new Replica();
                replicas.add(replica);
                System.out.println("Replica " + i + " added.");
            }

            // Assign the first replica as primary
            if (!replicas.isEmpty()) {
                primaryReplica = replicas.get(0);
                System.out.println("Primary Replica assigned.");
            }

            System.out.println("FrontEnd is ready.");
        } catch (Exception e) {
            System.err.println("Something went wrong here:");
            e.printStackTrace();
        }
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        try {
            if (primaryReplica.bid(userID, itemID, price)) {
                // Notify other replicas to update their state
                for (Replica replica : replicas) {
                    if (replica != primaryReplica) {
                        replica.updateStateFromPrimary(primaryReplica);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error during bid:");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int register(String email) throws RemoteException {
        return primaryReplica.register(email);
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        return primaryReplica.getSpec(itemID);
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        return primaryReplica.newAuction(userID, item);
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        return primaryReplica.listItems();
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        AuctionResult result = primaryReplica.closeAuction(userID, itemID);
        // Notify replicas to update their state after auction closure
        for (Replica replica : replicas) {
            if (replica != primaryReplica) {
                replica.updateStateFromPrimary(primaryReplica);
            }
        }
        return result;
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        return replicas.indexOf(primaryReplica);
    }

    public void addReplica(Replica replica) throws RemoteException {
        replicas.add(replica);
        System.out.println("Replica added with ID: " + replica.getPrimaryReplicaID());
    }

}
