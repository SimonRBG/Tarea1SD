package cl.uchile.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Simon on 14/10/2016.
 */
public interface IPoint extends Remote {

    public int getX() throws RemoteException;

    public int getY() throws RemoteException;

    public boolean getVisible() throws RemoteException;

}
