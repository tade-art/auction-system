import java.rmi.*;

/*
 * Interface class that contains the functions that are being ran on the server
 */
public interface Auction extends Remote {
    public AuctionItem getSpec(int itemID) throws RemoteException;
}