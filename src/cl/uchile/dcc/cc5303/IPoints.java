package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.HashSet;

public interface IPoints extends Remote{

    public void addPoint(Point po, Integer i) throws RemoteException;

    public HashSet[] getHashSet() throws RemoteException;

}
