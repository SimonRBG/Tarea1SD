package cl.uchile.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Stack;

/**
 * Created by pecesito on 13-11-16.
 */
public interface IComm extends Remote {


    public String getUrl_coordinator()throws RemoteException;

    public void setUrl_coordinator(String url_coordinator)throws RemoteException;

    public void addServer(String s)throws RemoteException;

    public boolean removeServer(String url_server)throws RemoteException;

    public String findNewServer()throws RemoteException;

    public void setMigrating(boolean b)throws RemoteException;

    public boolean getMigrating()throws RemoteException;

    public void migrate()throws RemoteException;

    public Boolean getServer_ready() throws RemoteException;

    public void setServer_ready(Boolean b) throws RemoteException;

    public String getActual_url_server() throws RemoteException;

    public void setActual_url_server(String actual_url_server) throws RemoteException;


}
