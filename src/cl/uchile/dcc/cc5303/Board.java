package cl.uchile.dcc.cc5303;

import java.awt.*;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Iterator;

public class Board extends Canvas{

    public int width, height;

    // Jugadores y elementos del juego acá
    public Player p1, p2;
    public LinkedHashSet<IPoint> points[];

    // doble buffer para dibujar
    public Image img;
    public Graphics buffer;


    public Board(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(Graphics graphics) {
        paint(graphics);
    }

    @Override
    public void paint(Graphics graphics) {
        if(this.buffer==null){
            this.img = createImage(getWidth(), getHeight());
            this.buffer = this.img.getGraphics();
        }

        this.buffer.setColor(Color.black);
        this.buffer.fillRect(0, 0, getWidth(), getHeight());

        // dibujar elementos del juego

        draw(points);
        graphics.drawImage(img, 0, 0, null);
    }

    private void draw(LinkedHashSet<IPoint>[] points){
        if(points==null)
                return;
        Color[] colors = new Color[5];
        colors[0] = Color.red;
        colors[1] = Color.blue;
        colors[2] = Color.yellow;
        colors[3] = Color.green;
        colors[4] = Color.cyan;
        //draw points for each Player
        for(int j = 0; j < points.length; j++) {//TODO change to 5 players
            buffer.setColor(colors[j]);
            LinkedHashSet<IPoint> l = points[j];
            Iterator itr = l.iterator();
            while (itr.hasNext()) {
                IPoint p = (IPoint) itr.next();


                try{
                    System.out.print(p.getX()+" "+p.getY()+" "+p.getVisible()+", ");
                    if (p.getVisible())
                        buffer.fillOval(p.getX() - Point.dHip / 2, p.getY() - Point.dHip / 2, Point.dHip, Point.dHip);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
            System.out.println("");
        }

    }
}
