import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class FrontEnd implements Auction {
    protected static ArrayList<Integer> replicaIDs = new ArrayList<>();

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
            System.err.println("Something went wrong with initliasing FrontEnd:");
            e.printStackTrace();
        }
    }

    // NEED TO ADD STATE SYNC SOMEWHERE HERE
    // CANNOT HANDLE IF PRIME REP ID 1 GOES DOWN AND COMES BACK, ISSUE WITH TRACKING
    // PRIME REP STATE

    protected static void connectToReplicas() throws RemoteException {
        IReplica currentPrimaryReplica = null;

        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            String[] registeredServices = registry.list();

            for (String service : registeredServices) {
                if (service.startsWith("Replica ")) {
                    try {
                        IReplica rep = (IReplica) registry.lookup(service);
                        int replicaID = rep.getReplicaID();

                        if (!replicaIDs.contains(replicaID))
                            replicaIDs.add(replicaID);

                        if (currentPrimaryReplica == null && replicaIDs.contains(replicaID)) {
                            currentPrimaryReplica = rep;
                            System.out.println("Primary replica assigned: Replica ID " + replicaID);
                        }

                    } catch (Exception e) {
                        System.err.println("Connection failed");

                        try {
                            int replicaID = Integer.parseInt(service.replace("Replica ", ""));
                            replicaIDs.remove((Integer) replicaID);

                            if (currentPrimaryReplica != null && replicaIDs.indexOf(replicaID) == 0) {
                                currentPrimaryReplica = null;

                                if (!replicaIDs.isEmpty()) {
                                    int newPrimaryID = replicaIDs.get(0);
                                    try {
                                        currentPrimaryReplica = (IReplica) registry.lookup("Replica " + newPrimaryID);
                                        System.out.println("Primary replica reassigned: Replica ID " + newPrimaryID);
                                    } catch (Exception ex) {
                                        System.err.println("Error reassigning primary replica: " + ex.getMessage());
                                    }
                                } else {
                                    System.err.println("No replicas available to assign as primary.");
                                }
                            }
                        } catch (NumberFormatException ex) {
                            System.err.println("Error parsing replica ID from service: " + ex.getMessage());
                        }
                    }
                }
            }

            if (currentPrimaryReplica != null) {
                currentPrimaryReplica.UpdateListInPrimaryReplica(replicaIDs);
            }

        }

        catch (Exception e) {
            System.err.println("Error connecting to replicas: " + e.getMessage());
        }

        System.out.println("Updated replica list: " + replicaIDs);
    }

    private IReplica getPrimaryReplica() throws RemoteException {
        if (replicaIDs.isEmpty())
            connectToReplicas();

        if (!replicaIDs.isEmpty()) {
            try {
                int primaryID = replicaIDs.get(0);
                return (IReplica) LocateRegistry.getRegistry("localhost").lookup("Replica " + primaryID);
            } catch (Exception e) {
                System.err.println("Error fetching primary replica:");
                e.printStackTrace();
            }
        }
        throw new RemoteException("No replicas available.");
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        connectToReplicas();
        return getPrimaryReplica().bid(userID, itemID, price);
    }

    @Override
    public int register(String email) throws RemoteException {
        connectToReplicas();
        return getPrimaryReplica().register(email);
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        connectToReplicas();
        return getPrimaryReplica().getSpec(itemID);
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        connectToReplicas();
        return getPrimaryReplica().newAuction(userID, item);
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        connectToReplicas();
        return getPrimaryReplica().listItems();
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        connectToReplicas();
        return getPrimaryReplica().closeAuction(userID, itemID);
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        connectToReplicas();
        return getPrimaryReplica().getReplicaID();
    }
}