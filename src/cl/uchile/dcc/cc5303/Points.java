package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Stack;

import static java.lang.Math.abs;

public class Points extends UnicastRemoteObject  implements IPoints {

    public int numplayers;
    public Stack ids = new Stack();
    public LinkedHashSet<IPoint> list[];// We have to keep in mind the order
    public int scores[];
    public boolean looses[];
    public boolean allLost;
    public boolean ready[];
    public boolean waitingEnd[];
    public int w, h;
    public HashMap<Integer, Integer> updateValue;
    public boolean waiting = false;
    boolean someOneQuit;
    public boolean someOneWaiting = false;

    public Object mutex2 = new Object();

    public Points(int n, int w, int h) throws RemoteException{
        synchronized (mutex2) {
            this.w = w;
            this.h = h;
            this.someOneQuit = false;
            this.scores = new int[n];
            this.looses = new boolean[n];
            this.ready = new boolean[n];
            this.allLost = false;
            this.numplayers = n;
            this.updateValue = new HashMap<Integer, Integer>();
            list = new LinkedHashSet[n];
            for (int i = 0; i < n; i++) {
                this.looses[i] = false;
                this.ready[i] = false;
                this.scores[i] = -1;
                list[i] = new LinkedHashSet<IPoint>();
                ids.push(n - 1 - i);
            }
        }
    }

    public void SetPoints(int[] scores, boolean[] looses, boolean allLost, boolean[] ready,LinkedHashSet<IPoint>[] l, Stack ids, int numplayers) throws RemoteException{
        synchronized (mutex2) {
            this.allLost = allLost;
            this.list = l;
            this.scores = scores;
            this.looses = looses;
            this.ready = ready;
            this.ids = ids;
            this.numplayers = numplayers;
        }
    }

    public Points getPoints() throws RemoteException{
        synchronized (mutex2) {
            return this;
        }

    }


    public void setUpdateValue(int ind) throws RemoteException {
        if (!updateValue.containsKey(ind))
            this.updateValue.put(ind,0);
        updateValue.put(ind, updateValue.get(ind)+1);
    }

    public int getUpdateValue(int ind) throws RemoteException {
        if (!updateValue.containsKey(ind))
            this.updateValue.put(ind,0);
        return  updateValue.get(ind);
    }

    public void addPoint(IPoint po, int i) throws RemoteException{

        if (po==null)
            return;
        // If the point doesn't choc with another one
        synchronized (mutex2) {
            if (check(po)) {
                list[i].add(po);
            } else {
                // Clear all the snake, save tha it lost and telling to the other to update their score
                list[i].clear();
                this.looses[i] = true;
                notify_score(i);
                notifyOperation("player " + i + " lost!!");
                if (checkAllPlayers()) {
                    this.allLost = true;
                    notifyOperation("all Lost");
                }
            }
        }

        notifyOperation("new Point Added"+po.getX()+", "+po.getY()+", "+po.getVisible()+". id: "+i);
    }

    public boolean lost(int id)throws RemoteException{
        synchronized (mutex2) {
            return looses[id];
        }

    }

    public boolean allLost() throws RemoteException{
        synchronized (mutex2) {
            return this.allLost;
        }

    }

    private boolean checkAllPlayers(){
            for (int i = 0; i < this.numplayers; i++) {
                if (!this.looses[i])
                    return false;
            }
            return true;
    }

    private boolean check(IPoint p) throws RemoteException {
            for (int i = 0; i < this.numplayers; i++) {
                Iterator it = ((LinkedHashSet) list[i].clone()).iterator();
                int px = p.getX();
                int py = p.getY();
                boolean pv = p.getVisible();
                // Check border
                // Handle the score boards
                if (px > w || px < w / 4 || py > h || py < 0) {
                    return false;
                }
                //Check other points
                while (it.hasNext()) {
                    try {
                        IPoint p2 = (IPoint) it.next();
                        if (abs(px - p2.getX()) < Point.dHip / 3 * 2 && abs(py - p2.getY()) < Point.dHip / 3 * 2 && (p2.getVisible() == pv && pv)) {
                            return false;
                        }
                        it.remove();
                    } catch (ConnectException e) {
                        // Free a space for new player
                        this.setQuit(i);
                        // Don't draw the player that we can't connect
                        break;
                    }
                }
            }
            return true;
    }

    public LinkedHashSet<IPoint>[] getList() throws RemoteException {
        synchronized (mutex2) {
            return list.clone();
        }
    }

    private void notify_score(int id) {
        // Update the score of all the others snakes alive
            for (int i = 0; i < numplayers; i++) {
                if (i != id && !this.looses[i])
                    scores[i]++;
            }
    }

    public int getScore(int id) throws RemoteException {
        synchronized (mutex2) {
            return scores[id];
        }
    }

    public int[] getScores() throws RemoteException {
        synchronized (mutex2) {
            return scores;
        }

    }

    public int getNumPlayers() throws RemoteException {
        synchronized (mutex2) {
            return numplayers;
        }
    }

    // Gave a new id for 5 users
    public int getId() throws RemoteException{
        synchronized (mutex2) {

            int id = (int) ids.peek();
            notifyOperation("new id " + id);
            scores[id] = 0;
            this.ready[id] = true;
            this.looses[id] = false;
            return (int) ids.pop();
        }
    }

    public void setWaitingResponse(boolean val) throws RemoteException {
        synchronized (mutex2) {
            this.waiting = val;
        }
    }

    public boolean getWaitingResponse() throws RemoteException {
        return waiting;
    }


    public boolean allPlayersReady() throws RemoteException{
        synchronized (mutex2) {
            for (int i = 0; i < numplayers; i++) {
                if (!ready[i])
                    return false;
            }
            return true;
        }
    }

    public void setReady(int id, boolean r, boolean l) throws RemoteException{
        synchronized (mutex2) {
            ready[id] = r;
            looses[id] = l;
            if (!l) {
                allLost = false;
            }
        }
    }

    public void setQuit(int id) throws  RemoteException{
        synchronized (mutex2) {
            list[id].clear();
            this.looses[id] = true;

            notify_score(id);
            if (checkAllPlayers()) {
                this.allLost = true;
                notifyOperation("all Lost");
            }
            // Id available & reinitilize the score
            ids.push(new Integer(id));
            this.scores[id] = -1;
            this.ready[id] = true;
            this.someOneQuit = true;
            notifyOperation("player " + id + " quit!!");
        }
    }

    public boolean someOneQuit() throws  RemoteException{
        synchronized (mutex2) {
            if (someOneQuit) {
                someOneQuit = false;
                return true;
            } else {
                return false;
            }
        }
    }

    public void setSomeOneWaiting(boolean value) throws RemoteException{
        this.someOneWaiting = value;
    }

    public boolean getSomeOneWaiiting() throws RemoteException{
        return someOneWaiting;
    }

    private void notifyOperation(String s){
        System.out.println("Operation: "+s);
    }
}
