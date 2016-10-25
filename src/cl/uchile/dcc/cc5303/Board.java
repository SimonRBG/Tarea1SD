package cl.uchile.dcc.cc5303;

import java.awt.*;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Iterator;

public class Board extends Canvas{

    public int width, height;

    // Jugadores y elementos del juego acá
    public Player p1, p2;
    public LinkedHashSet<IPoint> points[];
    public int scores[];
    public int numplayers;

    // doble buffer para dibujar
    public Image img;
    public Image score_board;
    public Graphics buffer;
    public Graphics buffer_score;

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
            this.score_board = createImage((int) (getWidth()*0.25), getHeight());
            this.buffer = this.img.getGraphics();
            this.buffer_score = this.score_board.getGraphics();
        }

        this.buffer.setColor(Color.black);
        this.buffer.fillRect(0, 0, getWidth(), getHeight());
        this.buffer_score.setColor(Color.DARK_GRAY);
        this.buffer_score.fillRect(0, 0, (int) (getWidth()*0.25), getHeight());


        // dibujar elementos del juego
        draw(points, scores, numplayers);

        graphics.drawImage(img, 0, 0, null);
        graphics.drawImage(score_board, 0, 0, null);

    }

    private void draw(LinkedHashSet<IPoint>[] points, int scores[], int numplayers){
        if(points==null) {
            return;
        }
        Color[] colors = new Color[5];
        colors[0] = Color.red;
        colors[1] = Color.blue;
        colors[2] = Color.yellow;
        colors[3] = Color.green;
        colors[4] = Color.cyan;

        // Ordering the player in function of the score
        int ind_p[] = new int[numplayers];
        int max_p[] = new int[numplayers];
        for (int j = 0; j < numplayers; j++) {
            for (int i = 0; i < numplayers ; i++) {
                if (max_p[i] <= scores[j]) {
                    for (int o = numplayers-1; o > i; o--) {
                        max_p[o] = max_p[o-1];
                        ind_p[o] = ind_p[o-1];
                    }
                    max_p[i] = scores[j];
                    ind_p[i] = j;
                    break;
                }
            }
        }
        for(int j = 0; j < numplayers; j++) {
            buffer.setColor(colors[j]);
            LinkedHashSet<IPoint> l = points[j];
            Iterator itr = l.iterator();
            while (itr.hasNext()) {
                IPoint p = (IPoint) itr.next();
                try{
                    if (p.getVisible())
                        buffer.fillOval(p.getX() - Point.dHip / 2, p.getY() - Point.dHip / 2, Point.dHip, Point.dHip);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        }
        // Draw the scores
        for (int i = 0; i < numplayers; i++) {
            buffer_score.setColor(colors[ind_p[i]]);
            buffer_score.drawString("Player " + ind_p[i] + " - " + max_p[i] + " pts" ,  1, 15*i+20 );
        }

    }
}
