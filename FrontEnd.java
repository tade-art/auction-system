import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class FrontEnd implements Auction {
    protected static ArrayList<Integer> replicaIDs = new ArrayList<>();
    protected static int amountOfReplicas = 3;
    protected static int primaryReplicaID;
    protected static IReplica primaryReplica = null;

    public static void main(String[] args) throws Exception {
        try {
            FrontEnd frontEnd = new FrontEnd();
            String name = "FrontEnd";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(frontEnd, 0);
            Registry registry = LocateRegistry.getRegistry("localhost");
            registry.rebind(name, stub);

            connectToReplicas();

            System.out.println("FrontEnd is ready.");
        } catch (Exception e) {
            System.err.println("Something went wrong here:");
            e.printStackTrace();
        }
    }

    // ______________
    // Helper methods
    // ______________

    protected static void connectToReplicas() throws RemoteException {
        for (int i = 1; i < amountOfReplicas; i++) {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost");
                IReplica rep = (IReplica) registry.lookup("Replica " + i);

                if (!replicaIDs.contains(rep.getReplicaID()))
                    replicaIDs.add(rep.getReplicaID());

                if (primaryReplica == null) {
                    primaryReplica = rep;
                    primaryReplicaID = primaryReplica.getReplicaID();
                }

            } catch (Exception e) {
                if (replicaIDs.contains(i)) {
                    replicaIDs.remove(Integer.valueOf(i));

                    if (primaryReplica != null && primaryReplicaID == i) {
                        primaryReplica = null;
                        if (!replicaIDs.isEmpty()) {
                            int newPrimaryID = replicaIDs.get(0);
                            try {
                                primaryReplica = (IReplica) LocateRegistry.getRegistry("localhost")
                                        .lookup("Replica " + newPrimaryID);
                                primaryReplicaID = newPrimaryID;
                            } catch (NotBoundException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        if (primaryReplica != null)
            primaryReplica.UpdateListInPrimaryReplica(replicaIDs);
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        connectToReplicas();
        primaryReplica.bid(userID, itemID, price);
        return false;
    }

    @Override
    public int register(String email) throws RemoteException {
        connectToReplicas();
        return primaryReplica.register(email);
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        connectToReplicas();
        return primaryReplica.getSpec(itemID);
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        connectToReplicas();
        return primaryReplica.newAuction(userID, item);
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        connectToReplicas();
        return primaryReplica.listItems();
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        connectToReplicas();
        return primaryReplica.closeAuction(userID, itemID);
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        connectToReplicas();
        return primaryReplica.getReplicaID();
    }
}
