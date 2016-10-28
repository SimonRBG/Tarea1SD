package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;



public class Server {
    //public static String URL_SERVER = "rmi://ip:1099/zatackaServer";
    public static final int w = 400, h = 300;



    public static void main(String[] args) {

        IPoints points;

        try{

            String ip = Util.getIp();
            System.out.println("serversIP: "+ip);
            String URL_SERVER = "rmi://ip:1099/zatackaServer";
            URL_SERVER=URL_SERVER.replace("ip",ip);
            System.out.println(URL_SERVER);

            // Line to solve rmiregistry Bug
            String hostname = ip;
            System.setProperty("java.rmi.server.hostname", hostname);

            // Parsing of the argument to launch with -n option
            int n = 2;
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-n")  && args[i+1]!=null){
                    n = Integer.parseInt(args[i+1]);
                    // Assume that we can't have negatives and >5 values
 		            if (n > 5){
			            n = 5;
		            }
		            if (n < 1){
			            n = 1;
		            }
		            break;
                }
            }
	    
            LocateRegistry.createRegistry(1099);
            points = new Points(n, w, h);
            Naming.rebind(URL_SERVER, points);
            System.out.println("Objeto points publicado en: " + URL_SERVER);

        }catch (RemoteException e){
            e.printStackTrace();
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }
}
