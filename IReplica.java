import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IReplica extends Auction {
    public void synchroniseState() throws RemoteException;

    public int getReplicaID() throws RemoteException;

    public void UpdateListInPrimaryReplica(ArrayList<Integer> replicas) throws RemoteException;

    public void clone(HashMap<Integer, List<AuctionItem>> items, HashMap<Integer, String> userIDList,
            HashMap<Integer, Integer> currHighestBidder) throws RemoteException;
}
