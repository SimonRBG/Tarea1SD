package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */
import java.util.HashSet;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class Points implements IPoints {

    HashSet<Point> list[];

    public Points(){
        list = new HashSet[5];
        for(int i = 0; i<5; i++) {
            list[i] = new HashSet<Point>();
        }
    }

    public void addPoint(Point po, Integer i) throws RemoteException{
        if (check(po)) {
            list[i].add(po);
        }
        else{
            list[i].clear();
        }
    }

    private boolean check(Point p) {
        for (int i = 0; i < 5; i++) {
            if (list[i].contains(p))
                return false;
        }
        return true;
    }

    public HashSet[] getHashSet() throws RemoteException{
        return list;
    }

}
