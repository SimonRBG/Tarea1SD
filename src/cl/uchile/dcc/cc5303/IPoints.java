package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;

public interface IPoints extends Remote{

    public void addPoint(IPoint po, int i) throws RemoteException;

    public LinkedHashSet<IPoint>[] getList() throws RemoteException;

    public int getId() throws RemoteException;

    public int getScore(int id) throws RemoteException;

    public int[] getScores() throws RemoteException;

    public int getNumPlayers() throws RemoteException;

    public boolean allPlayersReady() throws RemoteException;

    public void notify_score(int id) throws RemoteException;
	
    public boolean allLost() throws RemoteException;
	
    public void setReady(int id, boolean r) throws RemoteException;
}

