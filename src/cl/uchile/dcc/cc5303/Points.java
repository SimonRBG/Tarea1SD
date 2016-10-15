package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */
import java.util.ArrayList;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashSet;

import static java.lang.Math.abs;


public class Points extends UnicastRemoteObject  implements IPoints {

    // We have to keep in mind the order
    LinkedHashSet<IPoint> list[];
    public Points() throws RemoteException{
        list = new LinkedHashSet[5];
        for(int i = 0; i<5; i++) {
            list[i] = new LinkedHashSet<IPoint>();
        }
    }

    public void addPoint(IPoint po, int i) throws RemoteException{
        if (check(po)) {
            list[i].add(po);
        }
        else{
            list[i].clear();
        }
    }

    private boolean check(IPoint p) throws RemoteException {
        for (int i = 0; i < 5; i++) {
            for (IPoint p2 : list[i]) {
                // TODO : Change the evaluation criteria
                if (((abs(p.getX() - p2.getX()) < 3  || p.getY() == p2.getY()) && (abs(p.getY() - p2.getY()) < 3 && p.getX() == p2.getX())) && (p2.getVisible() == p.getVisible() && p.getVisible())) {
                   return false;
                }
            }
        }
        return true;
    }

    public LinkedHashSet<IPoint>[] getList() throws RemoteException {
        return list;
    }
}
