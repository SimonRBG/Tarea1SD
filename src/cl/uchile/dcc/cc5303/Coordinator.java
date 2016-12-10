package cl.uchile.dcc.cc5303;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by pecesito on 13-11-16.
 */
public class Coordinator {

    public static void main(String[] args){
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("filename.txt"), "utf-8"))) {
            writer.write("");
            System.out.println("deleting filename");
        }catch(IOException e){
            e.printStackTrace();
        }




        String port = "60000";
        String ip = Util.getIp();
        int portint = Integer.parseInt(port);
        IComm c;
        System.setProperty("java.rmi.server.hostname", ip);
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
