package cl.uchile.dcc.cc5303;

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
    String ip;
    String port;
    String url_coordinator;
    Boolean server_ready;
    String actual_url_server = null;
    boolean migrating;
    //Object mutex = new Object();

    public Comm(String ip, String port) throws RemoteException{
        urlServers = new ArrayList<String>();
        System.out.println(urlServers);
        server_ready = false;
        servers  = new ArrayList<Server>();
        this.ip = ip;
        this.port = port;
        migrating = false;
        url_coordinator = "rmi://ip:port/zatackaCoordinator";
        url_coordinator = url_coordinator.replace("ip",ip).replace("port",port);
        notifyOperation("new Comm created");
    }

    public Boolean getServer_ready() throws RemoteException{
        notifyOperation("getServer_ready:"+server_ready);
        return server_ready;
    }

    public void setServer_ready(Boolean server_ready) throws RemoteException{
        this.server_ready = server_ready;
        notifyOperation("setServer_ready:"+server_ready);
    }

    public String getUrl_coordinator() throws RemoteException{
        notifyOperation("getUrl_coordinator:"+url_coordinator);
        return url_coordinator;
    }

    public void setUrl_coordinator(String url_coordinator) throws RemoteException{
        notifyOperation("setUrl_coordinator:"+url_coordinator);
        this.url_coordinator = url_coordinator;
    }

    public String getActual_url_server() throws RemoteException{
        //notifyOperation("getActual_url_server:"+ actual_url_server);
        return actual_url_server;
    }

    public void setActual_url_server(String actual_url_server) throws RemoteException{
        notifyOperation("setActual_url_server:"+actual_url_server);
        this.actual_url_server = actual_url_server;
    }

    public void addServer(String url_server)throws RemoteException{
        urlServers.add(url_server);
        notifyOperation("addServer:"+url_server);
        if(urlServers.size()==1){
            server_ready = false;
            actual_url_server = url_server;
            notifyOperation("SetActualServer:"+url_server);
            //startServer(s);
        }
    }

    public boolean removeServer(String url_server)throws RemoteException{

        Iterator it = urlServers.iterator();
        while(it.hasNext()){
            String url = (String)it.next();
            if( url.indexOf(url_server) >= 0){
                urlServers.remove(url);
                notifyOperation("removeServer: "+ url);
                return true;
            }
        }
        notifyOperation("removeServer: server not found: "+ url_server);
        return false;
    }

    public String findNewServer()throws RemoteException{
        if(!urlServers.isEmpty()) {
            notifyOperation("findNewServer"+urlServers.get(0));
            return urlServers.get(0);
        }else {
            notifyOperation("findNewServer: no server found");
            return "";
        }
    }

    public void setMigrating(boolean b)throws RemoteException{
        migrating = b;
        notifyOperation("setMigrating"+b);
        if(b){
            migrate();
        }
    }

    public boolean getMigrating()throws RemoteException{
        notifyOperation("getMigrating:"+migrating);
        return migrating;
    }

    public void migrate()throws RemoteException{
        removeServer(actual_url_server);
        String new_server = findNewServer();
        server_ready = false;
        actual_url_server = new_server;
        notifyOperation("migrate");
    }

    public boolean lastServer() throws RemoteException {
        return urlServers.size() == 1;
    }

    private void notifyOperation(String s){
        System.out.println("Operation: "+s);
    }
}
