package cl.uchile.dcc.cc5303;

import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;

public class Player {

    public int angle;
    public boolean ended;
    public int id;
    public Point head;
    public int score;

    public Player(Point point, int id) {
        this.ended = false;
        this.head = point;
        this.id = id;
        this.score = 0;
    }

    public void moveUp() {
        this.angle = (this.angle + 10) % 360;
    }

    public void moveDown() {
        this.angle = (this.angle - 10) % 360;
    }

    public Point growUp(boolean visibility) throws RemoteException {
        if(!this.ended) {
            //Point head = this.body.get(this.body.size() - 1);
            int x = (int) (head.x + Point.dHip * Math.cos(Math.toRadians(this.angle)));
            int y = (int) (head.y + Point.dHip * Math.sin(Math.toRadians(this.angle)));

            head = new Point(x, y, visibility);
            // Update values for himself and tell it to the server
            return head;
        }
        return null;

    }

    @Override
    public String toString() {
        return this.angle + " " + this.head.x + " " + this.head.y;
    }


}
