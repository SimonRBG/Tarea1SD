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

    public static void main(String[] args) {

        IPoints points;

        try{
            // Line to solve rmiregistry Bug
            String hostname = "localhost";
            System.setProperty("java.rmi.server.hostname", hostname);
            LocateRegistry.createRegistry(1099);
            points = new Points();
            Naming.rebind(URL_SERVER, points);
            System.out.println("Objeto points publicado en: " + URL_SERVER);

        }catch (RemoteException e){
            e.printStackTrace();
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }
}