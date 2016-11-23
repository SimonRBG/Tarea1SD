package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Stack;

public interface IPoints extends Remote{

    public void SetPoints(int[] scores, boolean[] looses, boolean allLost, boolean[] ready,LinkedHashSet<IPoint>[] l, Stack ids, int numplayers)throws RemoteException;

    public Points getPoints() throws RemoteException;

    public  void addPoint(IPoint po, int i) throws RemoteException;

    public LinkedHashSet<IPoint>[] getList() throws RemoteException;

    public int getId() throws RemoteException;

    public int getScore(int id) throws RemoteException;

    public int[] getScores() throws RemoteException;

    public int getNumPlayers() throws RemoteException;

    public boolean allPlayersReady() throws RemoteException;
	
    public boolean allLost() throws RemoteException;

    public boolean lost(int id) throws RemoteException;
	
    public void setReady(int id, boolean r, boolean l) throws RemoteException;

    public void setQuit(int id) throws  RemoteException;

    public boolean someOneQuit() throws  RemoteException;
}

