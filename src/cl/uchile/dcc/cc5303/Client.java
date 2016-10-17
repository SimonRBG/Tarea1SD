package cl.uchile.dcc.cc5303;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;

/**
 * Created by pecesito on 17-10-16.
 */
public class Client extends Thread{

    public boolean[] keys;
    private final static String TITLE = "Juego - CC5303";
    public static IPoints remotePoints;

    private final static int WIDTH = 300, HEIGHT = 300;
    private final static int UPDATE_RATE = 30;
    private final static int GROW_RATE = 3;

    private JFrame frame;
    private Board tablero;
    private Player player;

    private int id;

    public Client() {
        try {
            keys = new boolean[KeyEvent.KEY_LAST];
            frame = new JFrame(TITLE);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            tablero = new Board(WIDTH, HEIGHT);
//            tablero.p1 = player;
            //tablero.p2 = player2;

            frame.add(tablero);
            tablero.setSize(WIDTH, HEIGHT);

            frame.pack();
            frame.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    keys[e.getKeyCode()] = true;
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    keys[e.getKeyCode()] = false;
                }
            });
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Recuperation of the shared object
            remotePoints = (IPoints) Naming.lookup(Server.URL_SERVER);
            try{
                id = remotePoints.getId();
            }catch(Exception e){
                e.printStackTrace();
                //TODO if not id-> no puede jugar pq ya hay 5 jugadores
            }


            //Player Initial position
            Random random = new Random();
            int posx = random.nextInt(WIDTH);
            int posy = random.nextInt(HEIGHT);
            player = new Player(new Point(posx, posy),id);

            int frames = 0;
            int skipFrames = 0;
            while (true) { // Main loop
                // Controles
                if (keys[KeyEvent.VK_UP]) {
                    System.out.println("UP");
                    player.moveUp();
                }
                if (keys[KeyEvent.VK_DOWN]) {
                    System.out.println("DOWN");
                    player.moveDown();
                }
                ++frames;

                if (frames == GROW_RATE){
                    Point new_point = null;
                    if (skipFrames-- > 0){
                        if (!player.ended) {
                            new_point = player.growUp(false);//returns the new point
                            remotePoints.addPoint(new_point, id);//add the new point
                        }
                    }else {
                        skipFrames = 0;
                        if (!player.ended) {
                            new_point = player.growUp(true);//add the new point
                            remotePoints.addPoint(new_point, id);//add the new point
                        }

                        if(random.nextFloat()< 0.1){
                            skipFrames = 2 + random.nextInt(4);
                            System.out.println(skipFrames);
                        }
                    }

                    frames = 0;
                }

                //TODO : Update values of others snakes

                // Recuperation of all the values
                LinkedHashSet<IPoint>[] pixels = remotePoints.getList();

                if (!player.ended) {
                    // Only looking at one snake for the moment
                    Iterator it = pixels[0].iterator();
                    int i = 0;
                    if (it.hasNext())
                        player.body.clear();
                    else if (player.body.size() > 1)
                        player.ended = true;
                    while(it.hasNext()) {
                        IPoint ptemp =(IPoint) it.next();
                        player.body.add(i, new Point(ptemp.getX(), ptemp.getY()));
                        i++;
                    }
                }

                // Tablero

                tablero.points = remotePoints.getList().clone();//pass the point to the board
                tablero.repaint();//paint the points in the board

                try {
                    this.sleep(5000 / UPDATE_RATE);
                } catch (InterruptedException ex) {

                }
            }
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

