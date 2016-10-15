package cl.uchile.dcc.cc5303;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Random;

public class MainThread extends Thread{

    public boolean[] keys;
    private final static String TITLE = "Juego - CC5303";
    public static IPoints remotePoints;

    private final static int WIDTH = 800, HEIGHT = 800;
    private final static int UPDATE_RATE = 30;
    private final static int GROW_RATE = 3;

    private JFrame frame;
    private Board tablero;
    private Player player1, player2;

    public MainThread() {
        try {
            keys = new boolean[KeyEvent.KEY_LAST];

            //Jugadores
            player1 = new Player(new Point(WIDTH / 3, 550));
            player2 = new Player(new Point(2 * WIDTH / 3, 550));

            frame = new JFrame(TITLE);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            tablero = new Board(WIDTH, HEIGHT);
            tablero.p1 = player1;
            tablero.p2 = player2;

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
        }  catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Recuperation of the shared object
            remotePoints = (IPoints) Naming.lookup(Server.URL_SERVER);
            int frames = 0;
            Random random = new Random();
            int skipFrames = 0;
            while (true) { // Main loop
                // Controles
                if (keys[KeyEvent.VK_UP]) {
                    tablero.p1.moveUp();
                }
                if (keys[KeyEvent.VK_DOWN]) {
                    tablero.p1.moveDown();
                }

                if (keys[KeyEvent.VK_W]) {
                    tablero.p2.moveUp();
                }
                if (keys[KeyEvent.VK_S]) {
                    tablero.p2.moveDown();
                }

                ++frames;
                if (frames == GROW_RATE){
                    if (skipFrames-- > 0){
                        if (!tablero.p1.ended)
                            tablero.p1.growUp(false);
                    //  tablero.p2.growUp(false);
                    }else {
                        skipFrames = 0;
                        if (!tablero.p1.ended)
                            tablero.p1.growUp(true);
                    //  tablero.p2.growUp(true);

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

                if (!tablero.p1.ended) {
                    // Only looking at one snake for the moment
                    Iterator it = pixels[0].iterator();
                    int i = 0;
                    if (it.hasNext())
                        tablero.p1.body.clear();
                    else if (tablero.p1.body.size() > 1)
                        tablero.p1.ended = true;
                    while(it.hasNext()) {
                        IPoint ptemp =(IPoint) it.next();
                        tablero.p1.body.add(i, new Point(ptemp.getX(), ptemp.getY()));
                        i++;
                    }
                }

                // Tablero
                tablero.repaint();

                try {
                    this.sleep(1000 / UPDATE_RATE);
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
