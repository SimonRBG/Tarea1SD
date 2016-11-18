package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.lang.management.ManagementFactory;
import java.util.Stack;

import com.sun.management.OperatingSystemMXBean;


public class Server extends Thread{
    //public static String url_server = "rmi://ip:1099/zatackaServer";
    public static final int w = 400, h = 300;

    String ip;
    String port;
    String url_server = "rmi://ip:port/zatackaServer";

    String ip_coord;
    String port_coord;

    int num_players;
    int portint;
    IPoints points;


    static IComm c;
    double charge_CPU;

    public Server(int n, String pc, String ipc, String p){
        ip = Util.getIp();
        port = p;
        url_server = url_server.replace("ip",ip);
        url_server = url_server.replace("port",port);
        num_players = n;
        portint = Integer.parseInt(port);
        port_coord = pc;
        ip_coord = ipc;
        System.setProperty("java.rmi.server.hostname", ip);
        try {
            LocateRegistry.createRegistry(portint);
            points = new Points(num_players, w, h);
            Naming.rebind(url_server, points);
        }catch(RemoteException e){
            e.printStackTrace();
        }catch(MalformedURLException e){
            e.printStackTrace();
        }

    }
    @Override
    public void run() {

        try{

            System.out.println("Objeto points publicado en: " + url_server);

            c.setServer_ready(true);

            com.sun.management.OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            while (true) {
                if (bean == null)
                    throw new NullPointerException("Unable to collect operating system metrics, jmx bean is null");

                try {
                    // We sleep to not surcharge the server's CPU charge
                    this.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                // TODO : Test on Linux if the same Method works
                // if (bean.getName().contains("Windows")) {
                    charge_CPU = bean.getSystemCpuLoad();
                    System.out.println("CPU charge : " + charge_CPU);
                // }
                //else {
                //    charge_CPU = bean.getSystemLoadAverage();
                 //   System.out.println("CPU charge : " + charge_CPU);
                //}
                // First step : First reason to migrate
                if (charge_CPU > 0.999) {
                    // then migrate to another server
                    synchronized (c.mutex) {
                        c.setMigrating(true);
                        try {
                            Points mypoints = points.getPoints();
                            String url = c.getActual_url_server();
                            System.out.println(url);
                            IPoints p = (IPoints) Naming.lookup(url);
                            p.SetPoints(mypoints.scores, mypoints.looses, mypoints.allLost, mypoints.ready, mypoints.list, mypoints.ids, mypoints.numplayers);
                        } catch (NotBoundException e) {
                            e.printStackTrace();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        c.setMigrating(false);
                    }
                    //System.exit(0);
                }
            }
          }catch (RemoteException e){
            e.printStackTrace();
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        int n = 2;
        String p = "60001";
        String pc = "60000";
        String ipc = Util.getIp();
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
            else if (args[i].equals("-p")  && args[i+1]!=null) {
                p = args[i+1];
            }
            else if (args[i].equals("-pc")  && args[i+1]!=null) {
                pc = args[i+1];
            }
            else if (args[i].equals("-ipc")  && args[i+1]!=null) {
                ipc = args[i+1];
            }
        }

        //s.start();
        Server s = new Server(n, pc, ipc, p);
        String url_coordinator = "rmi://ip:port/zatackaCoordinator";
        url_coordinator = url_coordinator.replace("port",pc).replace("ip", ipc);
        try{
            c = (IComm) Naming.lookup(url_coordinator);
            System.out.println(s.url_server);
            registerWithCoordinator(c, s.url_server);

            while(true){
                synchronized (c.mutex) {
                    if (c.getActual_url_server().compareTo(s.url_server) == 0 && !c.getMigrating()) {
                        System.out.println("1");
                        if (!c.getServer_ready()) {
                            System.out.println("2");
                            if (!s.isAlive()) {
                                System.out.println("3");
                                s.start();
                                break;

                            }
                        }
                    }
                }
                //TODO revisar el canal de comunicación periódicamente??
                //TODO revisar si hay que migrar y dar la orden al Server y avisar al coordinador
            }

            //while(true){
            //    if(c.getMigrating()){

            //    }
            //}
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }




    }

    public static void registerWithCoordinator(IComm c, String s) throws RemoteException{
        c.addServer(s);
    }



/*
    public static void main(String[] args) {



        try{

            String ip = Util.getIp();
            String port = "1099";
            System.out.println("serversIP: "+ip);
            url_server=url_server.replace("ip",ip);
            url_server=url_server.replace("port",port);
            System.out.println(url_server);

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

            int portint = Integer.parseInt(port);
            LocateRegistry.createRegistry(portint);
            points = new Points(n, w, h);
            Naming.rebind(url_server, points);
            System.out.println("Objeto points publicado en: " + url_server);

        }catch (RemoteException e){
            e.printStackTrace();
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }
    */
}
