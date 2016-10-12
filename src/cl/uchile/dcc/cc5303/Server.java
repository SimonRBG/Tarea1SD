package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class Server {
    public static final String URL_SERVER = "rmi://localhost:1099/zatackaServer";

    public static void main(String[] args) {

        IPoints points;

        try{
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