package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Stack;

public interface IPoints extends Remote{

    public void SetPoints(int[] scores, boolean[] looses, boolean allLost, boolean[] ready,LinkedHashSet<Point>[] l, Stack ids, int numplayers, boolean someOneQuit, boolean waiting, boolean someOneWaiting, HashMap<Integer, Integer> updateValue, boolean gamePaused ) throws RemoteException;

    public Points getPoints() throws RemoteException;

    public void setUpdateValue (int ind) throws RemoteException;

    public int getUpdateValue (int ind) throws RemoteException;

    public void setWaitingResponse(boolean val) throws RemoteException;

    public boolean getWaitingResponse() throws RemoteException;

    public  boolean addPoint(Point po, int i) throws RemoteException;

    public LinkedHashSet<Point>[] getList() throws RemoteException;

    public int getId() throws RemoteException;

    public int getId(int id) throws RemoteException;

    public int getScore(int id) throws RemoteException;

    public int[] getScores() throws RemoteException;

    public boolean[] getLooses() throws RemoteException;

    public void notLost(int id) throws RemoteException;

    public int getNumPlayers() throws RemoteException;

    public boolean allPlayersReady() throws RemoteException;
	
    public boolean allLost() throws RemoteException;

    public boolean lost(int id) throws RemoteException;
	
    public void setReady(int id, boolean r, boolean l) throws RemoteException;

    public void setQuit(int id, boolean voluntary) throws  RemoteException;

    public void setSomeOneWaiting(boolean value) throws RemoteException;

    public boolean getSomeOneWaiiting() throws RemoteException;

    public boolean someOneQuit() throws  RemoteException;

    public boolean gamePaused() throws RemoteException;

    public boolean setPause() throws RemoteException;
}

