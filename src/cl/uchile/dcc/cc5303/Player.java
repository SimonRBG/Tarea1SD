package cl.uchile.dcc.cc5303;

import java.awt.*;
import java.util.ArrayList;

public class Player {

    public int angle;
    public ArrayList<Point> body;

    public Player(Point point) {
        this.body = new ArrayList<Point>();
        this.body.add(point);
    }

    public void draw(Graphics graphics) {
        for (Point p : this.body)
            if (p.visible)
                graphics.fillOval(p.x - Point.dHip/2, p.y - Point.dHip/2, Point.dHip, Point.dHip);
    }

    public void moveUp() {
        this.angle = (this.angle + 10) % 360;
    }

    public void moveDown() {
        this.angle = (this.angle - 10) % 360;
    }

    public void growUp(boolean visibility) {
        Point head = this.body.get(this.body.size() - 1);
        int x = (int) (head.x + Point.dHip*Math.cos(Math.toRadians(this.angle)));
        int y = (int) (head.y + Point.dHip*Math.sin(Math.toRadians(this.angle)));
        this.body.add(new Point(x,y, visibility));
    }

    @Override
    public String toString() {
        return this.angle + " " + this.body.get(0).x + " " + this.body.get(0).y;
    }
}
