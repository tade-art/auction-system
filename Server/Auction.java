import java.rmi.*;

public interface Auction extends Remote {
    public AuctionItem getSpec(int itemID) throws RemoteException;
}