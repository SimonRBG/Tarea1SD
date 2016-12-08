package cl.uchile.dcc.cc5303;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Point implements Serializable {
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

    private void writeObject(java.io.ObjectOutputStream out) throws IOException{
        //perform the default serialization for all non-transient, non-static fields
        out.defaultWriteObject();
    }

    @Override
    public String toString() {
        String p = new StringBuffer().append(this.x).append(" ")
                .append(this.y).append(" ")
                .append(this.visible?1:0).toString();
        //System.out.println("Point: "+p);
        return p;
    }


    public Point(String s){
        String[] sa = s.split(" ");
        try {
            x = Integer.parseInt(sa[0]);
            y = Integer.parseInt(sa[1]);
            visible = (Integer.parseInt(sa[2]) == 1) ? true : false;
            System.out.println("Point: "+x +","+y+","+visible+";");
        }catch(NumberFormatException e){
            System.out.println("not a number: "+ sa);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public int compareTo(Point p){
        if (this == p){
            return 0;
        }
        else if(this.x == p.x) {
            if (this.y == p.y) {
                if (this.visible) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (this.y < p.y) {
                return -1;
            } else {
                return 1;
            }
        }else if(this.x<this.y){
            return -1;
        }else {
            return 1;
        }
    }
}

