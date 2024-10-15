import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
 * Class that contains client information - connects to server and runs .getSpec()
 */

public class Client{
     public static void main(String[] args) {
       if (args.length < 1) {
       System.out.println("In client");
       return;
       }

         int n = Integer.parseInt(args[0]);
         
         //Process to connect to server - gets name and connect on localhost (127.0.0.1)
         try {
               String name = "Auction Server";
               Registry registry = LocateRegistry.getRegistry("localhost");
               IAuction server = (IAuction) registry.lookup(name);
  
               //Line where info about AuctionItem object is being gotten from
               AuctionItem toReturn = server.getSpec(n);
               System.out.println("object returned: " + toReturn);
              }
              catch (Exception e) {
               System.err.println("Exception:");
               e.printStackTrace();
               }
      }
}
