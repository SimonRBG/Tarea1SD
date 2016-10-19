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

    // We have to keep in mind the order
    LinkedHashSet<IPoint> list[];
    Stack ids = new Stack();

    public Points() throws RemoteException{
        list = new LinkedHashSet[5];
        for(int i = 0; i<5; i++) {
            list[i] = new LinkedHashSet<IPoint>();
            ids.push(4-i);
        }
    }

    public void addPoint(IPoint po, int i) throws RemoteException{
        if (check(po)) {
            list[i].add(po);
        }
        else{
            list[i].clear();
            notifyOperation("player "+i+" lost!!");
        }
        notifyOperation("new Point Added"+po.getX()+", "+po.getY()+", "+po.getVisible()+". id: "+i);
    }

    private boolean check(IPoint p) throws RemoteException {
        for (int i = 0; i < 5; i++) {
            // TODO : Change the evaluation criteria
            Iterator it = ((LinkedHashSet) list[i].clone()).iterator();
            int px = p.getX();
            int py = p.getY();
            boolean pv = p.getVisible();
            while (it.hasNext()) {
                IPoint p2 = (IPoint) it.next();
                if ( abs(px- p2.getX()) < Point.dHip/2 && abs(py - p2.getY())<Point.dHip/2  && (p2.getVisible() == pv && pv)){
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

    //gave a new id for 5 users
    public int getId() throws RemoteException{
        notifyOperation("new id"+ids.peek());
        return (int)ids.pop();
    }
    private void notifyOperation(String s){
        System.out.println("Operation: "+s);
    }
}
