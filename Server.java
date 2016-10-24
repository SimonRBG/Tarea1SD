package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Server {
    public static final String URL_SERVER = "rmi://localhost:1099/zatackaServer";
    public static final int w = 300, h = 300;


    public static void main(String[] args) {

        IPoints points;

        try{
            // Line to solve rmiregistry Bug
            String hostname = "localhost";
            System.setProperty("java.rmi.server.hostname", hostname);

            // Parsing of the argument to launch with -n option
            int n = 2;
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-n")  && !args[i+1].isEmpty()){
                    n = Integer.getInteger(args[i+1]);
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
