package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Stack;

import static java.lang.Math.abs;


public class Points extends UnicastRemoteObject  implements IPoints {

    int numplayers;
    // We have to keep in mind the order
    LinkedHashSet<IPoint> list[];
    int scores[];
    boolean looses[];
    boolean allLost;
    int w, h;

    Stack ids = new Stack();

    boolean ready[];

    public Points(int n, int w, int h) throws RemoteException{
	    this.w=w;
	    this.h=h;
        this.scores = new int[n];
        this.looses = new boolean[n];
        this.ready = new boolean[n];
	this.allLost = false;
        this.numplayers = n;
        list = new LinkedHashSet[n];
        for(int i = 0; i<n; i++) {
            this.looses[i] = false;
	    this.ready[i] = false;
            list[i] = new LinkedHashSet<IPoint>();
            ids.push(n-1-i);
        }
    }

    public void addPoint(IPoint po, int i) throws RemoteException{
        // If the point doesn't choc with another one
        if (check(po)) {
            list[i].add(po);
        }
        else{
            // Clear all the snake, save tha it lost and telling to the other to update their score
            list[i].clear();
            this.looses[i] = true;
            notify_score(i);
            notifyOperation("player "+i+" lost!!");
	    if(checkAllPlayers()){
		this.allLost=true;
		notifyOperation("all Lost");
		//TODO borrar todo y empezar de nuevo	
	    }
        }
        notifyOperation("new Point Added"+po.getX()+", "+po.getY()+", "+po.getVisible()+". id: "+i);
    }
	
    public boolean allLost() throws RemoteException{
	return this.allLost;
    }

    private boolean checkAllPlayers(){
	for (int i = 0; i < this.numplayers; i++){
	    if(!this.looses[i])
		return false;
	}
	return true;
    }

    private boolean check(IPoint p) throws RemoteException {
        for (int i = 0; i < this.numplayers; i++) {
            // TODO : Change the evaluation criteria
            Iterator it = ((LinkedHashSet) list[i].clone()).iterator();
            int px = p.getX();
            int py = p.getY();
            boolean pv = p.getVisible();
            // Check border
            // Handle de score boards
            if(px > w || px < w/4 || py > h || py < 0){
                return false;
            }
            //Check other points
            while (it.hasNext()) {
                IPoint p2 = (IPoint) it.next();
                if ( abs(px- p2.getX()) < Point.dHip/2 && abs(py - p2.getY())<Point.dHip/2  && (p2.getVisible() == pv && pv)) {
                    return false;
                }
                it.remove();
            }
        }
        return true;
    }

    public LinkedHashSet<IPoint>[] getList() throws RemoteException {
        notifyOperation("getList");
        return list.clone();
    }

    public void notify_score(int id) throws RemoteException {
        // Update the score of all the others snakes alive
        for (int i = 0; i < numplayers; i++) {
            if (i != id && !this.looses[i])
                scores[i]++;
        }
    }

    public int getScore(int id) throws RemoteException {
        return scores[id];
    }

    public int[] getScores() throws RemoteException {
        return scores;
    }

    public int getNumPlayers() throws RemoteException {
        return numplayers;
    }

    // Gave a new id for 5 users
    public int getId() throws RemoteException{
        int id = (int)ids.peek();
        notifyOperation("new id "+id);
	ready[id]=true;        
        return (int)ids.pop();
    }

    public boolean allPlayersReady() throws RemoteException{
	for (int i = 0; i < numplayers; i++) {
            if(!ready[i])
		return false;
        }
        return true;
    }

    public void setReady(int id, boolean r, boolean l) throws RemoteException{
	ready[id]=r;
	looses[id] = l;
    }

    private void notifyOperation(String s){
        System.out.println("Operation: "+s);
    }
}
