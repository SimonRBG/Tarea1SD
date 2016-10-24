package cl.uchile.dcc.cc5303;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Point extends UnicastRemoteObject implements IPoint {
    public int x,y;
    public boolean visible;

    public static final int dHip = 4;

    public Point(int x, int y) throws RemoteException{
        this.x = x;
        this.y = y;
        this.visible = true;
    }

    public Point(int x, int y, boolean visibility) throws RemoteException{
        this.x = x;
        this.y = y;
        this.visible = visibility;
    }

    public int getX() throws RemoteException {
        return x;
    }

    public int getY() throws RemoteException {
        return y;
    }

    public boolean getVisible() throws RemoteException {
        return visible;
    }
}

