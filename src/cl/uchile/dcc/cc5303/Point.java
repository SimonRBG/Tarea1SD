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
        return new StringBuffer("").append(this.x).append(" ")
                .append(this.y).append(" ")
                .append(this.visible?1:0).toString();
    }


    public Point(String s){
        String[] sa = s.trim().split(" ");
        System.out.println(s);
        System.out.println(sa[0]+" "+sa[1]+ " "+ sa[2]);
        try {
            this.x = Integer.parseInt(sa[0].trim());
            this.y = Integer.parseInt(sa[1].trim());
            this.visible = (Integer.parseInt(sa[2].trim()) == 1) ? true : false;
        }catch(NumberFormatException e){
            e.printStackTrace();
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

