package cl.uchile.dcc.cc5303;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pecesito on 13-11-16.
 */
public class Comm extends UnicastRemoteObject implements IComm{

    List<Server> servers;
    List<String> urlServers;
    double chargeActualServer;
    List<Double> chargeNewServer;
    String ip;
    String port;
    String url_coordinator;
    Boolean server_ready;
    String actual_url_server = null;
    boolean migrating;
    Object mutex2 = new Object();

    public Comm(String ip, String port) throws RemoteException{
        synchronized (mutex2) {
            urlServers = new ArrayList<String>();
            chargeNewServer = new ArrayList<Double>();
            System.out.println(urlServers);
            server_ready = false;
            servers = new ArrayList<Server>();
            this.ip = ip;
            this.port = port;
            migrating = false;
            url_coordinator = "rmi://ip:port/zatackaCoordinator";
            url_coordinator = url_coordinator.replace("ip", ip).replace("port", port);
            notifyOperation("new Comm created");
        }
    }

    public Boolean getServer_ready() throws RemoteException{
        synchronized (mutex2) {
            notifyOperation("getServer_ready:" + server_ready);
            return server_ready;
        }
    }

    public void setServer_ready(Boolean server_ready) throws RemoteException{
        synchronized (mutex2) {
            this.server_ready = server_ready;
            notifyOperation("setServer_ready:" + server_ready);
        }
    }

    public String getUrl_coordinator() throws RemoteException{
        synchronized (mutex2) {
            notifyOperation("getUrl_coordinator:" + url_coordinator);
            return url_coordinator;
        }
    }

    public void setUrl_coordinator(String url_coordinator) throws RemoteException{
        synchronized (mutex2) {
            notifyOperation("setUrl_coordinator:" + url_coordinator);
            this.url_coordinator = url_coordinator;
        }
    }

    public void setChargeActualServer(double charge) throws RemoteException{
        synchronized (mutex2) {
            this.chargeActualServer = charge;
        }
    }

    public double getChargeActualServer() throws RemoteException{
        synchronized (mutex2) {
            return chargeActualServer;
        }
    }

    public String getActual_url_server() throws RemoteException{
        synchronized (mutex2) {
            //notifyOperation("getActual_url_server:"+ actual_url_server);
            return actual_url_server;
        }
    }

    public void setActual_url_server(String actual_url_server) throws RemoteException{
        synchronized (mutex2) {
            notifyOperation("setActual_url_server:" + actual_url_server);
            this.actual_url_server = actual_url_server;
        }
    }

    public void addChargeServer (int index, double charge ) throws RemoteException{
        synchronized (mutex2) {
            chargeNewServer.add(index, charge);
        }
    }


    public int addServer(String url_server)throws RemoteException{
        synchronized (mutex2) {
            if(urlServers.contains(url_server)){
                server_ready = false;
            }else {
                urlServers.add(url_server);
                notifyOperation("addServer:" + url_server);
                if (urlServers.size() == 1) {
                    server_ready = false;
                    actual_url_server = url_server;
                    notifyOperation("SetActualServer:" + url_server);
                    //startServer(s);
                }
            }
            return urlServers.size() - 1;
        }
    }

    public boolean removeServer(String url_server)throws RemoteException{
        synchronized (mutex2) {
            Iterator it = urlServers.iterator();
            while (it.hasNext()) {
                String url = (String) it.next();
                if (url.indexOf(url_server) >= 0) {
                    urlServers.remove(url);
                    notifyOperation("removeServer: " + url);
                    return true;
                }
            }
            notifyOperation("removeServer: server not found: " + url_server);
            return false;
        }
    }

    public String findNewServer()throws RemoteException{
        synchronized (mutex2) {
            if (!urlServers.isEmpty()) {
                int serverFound = 0;
                double min_charge = 5.0;
                int i = 0;
                // Selecting the server with the minimal charge
                while (true) {

                    while (i < urlServers.size()) {
                        if (!urlServers.get(i).contains(actual_url_server)) {
                            if (chargeNewServer.get(i) <= min_charge) {
                                min_charge = chargeNewServer.get(i);
                                serverFound = i;
                            }
                        }
                        i++;
                    }
                    if (isAvailable(urlServers.get(serverFound)))
                        break;
                    else {
                        urlServers.remove(serverFound);
                        i = 0;
                        min_charge = 1.0;
                        serverFound = 0;
                    }
                }

                notifyOperation("findNewServer" + urlServers.get(serverFound));
                return urlServers.get(serverFound);
            } else {
                notifyOperation("findNewServer: no server found");
                return "";
            }
        }
    }

    private boolean isAvailable(String url_server){
        synchronized (mutex2) {
            try {
                IPoints p = (IPoints) Naming.lookup(url_server);
                return true;
            } catch (RemoteException e) {
                notifyOperation("not available Remote");
                return false;
            } catch (NotBoundException e) {
                notifyOperation("not available not bound");
                return false;
            } catch (MalformedURLException e) {
                notifyOperation("not available Malformed URL");
                return false;
            }
        }
    }

    public void setMigrating(boolean b)throws RemoteException{
        synchronized (mutex2) {
            migrating = b;
            notifyOperation("setMigrating" + b);
            if (b) {
                migrate();
            }
        }
    }

    public boolean getMigrating()throws RemoteException{
        synchronized (mutex2) {
            notifyOperation("getMigrating:" + migrating);
            return migrating;
        }
    }

    public void migrate()throws RemoteException{
        synchronized (mutex2) {
            String new_server = findNewServer();
            server_ready = false;
            actual_url_server = new_server;
            notifyOperation("migrate");
        }
    }

    public boolean lastServer() throws RemoteException {
        synchronized (mutex2) {
            return urlServers.size() == 1;
        }
    }

    private void notifyOperation(String s){
        System.out.println("Operation: "+s);
    }
}
