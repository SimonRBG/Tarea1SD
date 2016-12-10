package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
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
    int index;

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

            loadState();

            System.out.println("Objeto points publicado en: " + url_server);

            int old_size[]= new int[points.getNumPlayers()];

            c.setServer_ready(true);

            com.sun.management.OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

            //int save = 11;
            ArrayList<Integer> old_values = new ArrayList<>();
            for (int i=0; i < points.getNumPlayers(); i++) {
                old_values.add(0);
            }
            while (true) {
                if (bean == null)
                    throw new NullPointerException("Unable to collect operating system metrics, jmx bean is null");

                //if(save >= 3){
                    saveState();
                //    save = 0;
                //}
               // save ++;
                try {
                    // We sleep to not surcharge the server's CPU charge
                    this.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                charge_CPU = bean.getSystemCpuLoad();
                System.out.println("CPU charge : " + charge_CPU);
                // Verify that any client is still working and if not, make the quit for them
                int o = 0;

                // Case in which they all wait for begining other game
                if (points.getWaitingResponse()) {
                    while (o < points.getNumPlayers()) {
                        if (old_values.get(o) == points.getUpdateValue(o) && points.getUpdateValue(o) != 0) {
                            System.out.println("waiting response and quit"+ o);
                            points.setQuit(o);
                        }
                        old_values.set(o, points.getUpdateValue(o));
                        o++;
                    }
                }

                // Case in which some of the loosers are waiting for the game to end
                if (points.getSomeOneWaiiting()) {
                    while (o < points.getNumPlayers()) {
                        System.out.println(String.valueOf(old_size[o] + " - " + points.getList()[o].size()));
                        // If there is not more points for a given player and that player doesn't lost : he has quit the game
                        if (old_size[o] == points.getList()[o].size() && !points.lost(o)){
                            System.out.println("waiting response and quit 2222");
                            points.setQuit(o);
                        }
                        old_size[o] =points.getList()[o].size();
                        o++;
                    }
                }

                // First step : First reason to migrate : CPU_charge. Second reason : someOneQuit
                if ((charge_CPU > 0.75 && ! c.lastServer()) || (points.someOneQuit() && ! c.lastServer())){
                    // then migrate to another server
                    if (charge_CPU > 0.75) {
                        System.out.println("migrate because of the CPU charge");
                    } else {
                        System.out.println("migrate because someone has quitted the game");
                    }
                    synchronized (c.mutex) {
                        c.setChargeActualServer(charge_CPU);
                        c.setMigrating(true);
                        try {
                            Points mypoints = points.getPoints();
                            String url = c.getActual_url_server();
                            System.out.println(url);
                            IPoints p = (IPoints) Naming.lookup(url);
                            p.SetPoints(mypoints.scores, mypoints.looses, mypoints.allLost, mypoints.ready, mypoints.list, mypoints.ids, mypoints.numplayers);
                            Naming.unbind(url_server);
                            points = new Points(num_players, w, h);
                            Naming.rebind(url_server, points);
                        } catch (NotBoundException e) {
                            e.printStackTrace();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }  catch (ClassCastException e) {
                            System.err.println("No server found");
                            System.exit(1);
                        }
                        c.setMigrating(false);
                    }
                    this.waitToBeElected(false);
                }
            }
          }catch (RemoteException e){
            e.printStackTrace();
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        int n = 2;
        double charge_CPU;
        String p = "60003";
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
            s.index = registerWithCoordinator(c, s.url_server);
            s.waitToBeElected(true);

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }




    }

    public void waitToBeElected(boolean firstTime) {
        com.sun.management.OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        if (bean == null)
            throw new NullPointerException("Unable to collect operating system metrics, jmx bean is null");

        while (true) {
            try {
                synchronized (c.mutex) {
                    try {
                        // We sleep to not surcharge the server's CPU charge
                        sleep(1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    this.charge_CPU = bean.getSystemCpuLoad();
                    System.out.println("CPU charge (waiting to be elected) : " + this.charge_CPU);
                    c.addChargeServer(this.index, this.charge_CPU);
                    if (c.getActual_url_server().compareTo(this.url_server) == 0 && !this.c.getMigrating()) {
                        System.out.println("1");
                        if (!c.getServer_ready()) {

                            System.out.println("2");
                            if (firstTime)
                                this.start();
                            else
                                c.setServer_ready(true);
                            break;

                            }
                        }
                    }

            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    public static int registerWithCoordinator(IComm c, String s) throws RemoteException{
        return c.addServer(s);
    }

    public void saveState() {
        synchronized (c.mutex) {
            try {

                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("filename.txt"), "utf-8"))) {
                    writer.write(points.toString());
                    System.out.println("Writing");
                }


            /*FileOutputStream fout = new FileOutputStream("filename.txt", false);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject((Points)points);
            oos.close();
            System.out.println("Writing...");
            */
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void loadState() {
        synchronized (c.mutex) {
            try {
                BufferedReader br = new BufferedReader(new FileReader("filename.txt"));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                if(line != null && !line.isEmpty()) {
                    System.out.println(line);
                    Points ps = new Points(line, w, h);
                    if (ps != null) {
                        points = ps;
                        Naming.rebind(url_server, points);
                    }
                    else{
                        System.out.println("null points: "+ line);
                    }
                }

                br.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
