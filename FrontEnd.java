import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FrontEnd implements Auction {
    protected static HashMap<Integer, Boolean> replicaStatus = new HashMap<>();

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
            System.err.println("Something went wrong with initializing FrontEnd:");
            e.printStackTrace();
        }
    }

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

                        replicaStatus.putIfAbsent(replicaID, false);

                        if (currentPrimaryReplica == null && !replicaStatus.containsValue(true)) {
                            currentPrimaryReplica = rep;
                            replicaStatus.put(replicaID, true);
                        }

                        if (currentPrimaryReplica != null)
                            currentPrimaryReplica.synchroniseState();

                    } catch (Exception e) {
                        System.err.println("Connection failed to " + service);

                        try {
                            int replicaID = Integer.parseInt(service.replace("Replica ", ""));
                            replicaStatus.remove(replicaID);

                            if (currentPrimaryReplica != null && replicaStatus.containsKey(replicaID)
                                    && replicaStatus.get(replicaID)) {
                                currentPrimaryReplica = null;

                                for (Map.Entry<Integer, Boolean> entry : replicaStatus.entrySet()) {
                                    if (!entry.getValue()) {
                                        try {
                                            currentPrimaryReplica = (IReplica) registry
                                                    .lookup("Replica " + entry.getKey());
                                            replicaStatus.put(entry.getKey(), true);
                                            break;
                                        } catch (Exception ex) {
                                            System.err.println("Error reassigning primary replica: " + ex.getMessage());
                                        }
                                    }
                                }
                            }
                        } catch (NumberFormatException ex) {
                            System.err.println("Error parsing replica ID from service: " + ex.getMessage());
                        }
                    }
                }
            }
            if (currentPrimaryReplica != null)
                currentPrimaryReplica.UpdateListInPrimaryReplica((ArrayList<Integer>) replicaStatus.keySet());
        } catch (Exception e) {
            System.err.println("Error connecting to replicas: " + e.getMessage());
        }
    }

    private IReplica getPrimaryReplica() throws RemoteException {
        if (replicaStatus.isEmpty())
            connectToReplicas();

        for (Map.Entry<Integer, Boolean> entry : replicaStatus.entrySet()) {
            if (entry.getValue()) {
                try {
                    return (IReplica) LocateRegistry.getRegistry("localhost").lookup("Replica " + entry.getKey());
                } catch (Exception e) {
                    System.err.println("Error fetching primary replica:");
                    e.printStackTrace();
                }
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