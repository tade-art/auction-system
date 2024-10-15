import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/*
 * Server class that contains server information as well as function implementations
 */
public class Server implements IAuction{
    public Server() {
        super();
    }
    
    public static void main(String[] args) {
        try {
         Server s = new Server();
         String name = "Auction Server";
         IAuction stub = (IAuction) UnicastRemoteObject.exportObject(s, 0);
         Registry registry = LocateRegistry.getRegistry();
         registry.rebind(name, stub);
         System.out.println("Server ready");
        } 
        
        catch (Exception e) {
         System.err.println("Exception:");
         e.printStackTrace();
        }

        //Create a list of auction items to check against - TODO
    }

    /*
     * Implementation of the getSpec function - TODO
     */
    public AuctionItem getSpec(int itemID) throws RemoteException {
        System.out.println("In get spec");
        return null;
    } 
  }
  