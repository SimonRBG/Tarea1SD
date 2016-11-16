package cl.uchile.dcc.cc5303;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by pecesito on 13-11-16.
 */
public class Coordinator {

    public static void main(String[] args){
        String port = "60000";
        String ip = Util.getIp();
        int portint = Integer.parseInt(port);
        IComm c;
        try{
            LocateRegistry.createRegistry(portint);
            c = new Comm(ip, port);
            Naming.rebind(c.getUrl_coordinator(), c);
            System.out.println("Objeto points publicado en: " + c.getUrl_coordinator());
        }catch (RemoteException e){
            e.printStackTrace();
        }catch (MalformedURLException e){
            e.printStackTrace();
        }

    }

}
