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

public class Client extends Thread{
/**
 * Created by pecesito on 17-10-16.
 */

    public boolean[] keys;
    private final static String TITLE = "Juego - CC5303";
    public static IPoints remotePoints;

    private int w, h;
    private final static int UPDATE_RATE = 30;
    private final static int GROW_RATE = 3;

    private JFrame frame;
    private Board tablero;
    private Player player;

    private int id;

    public Client() {
        try {
	    w = Server.w;
	    h = Server.h;
            keys = new boolean[KeyEvent.KEY_LAST];
            frame = new JFrame(TITLE);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            tablero = new Board(w, h);

            frame.add(tablero);
            tablero.setSize(Server.w, Server.h);

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

            String hostname = "172.17.69.196";
            System.setProperty("java.rmi.server.hostname", hostname);

            // Recuperation of the shared object
            remotePoints = (IPoints) Naming.lookup(Server.URL_SERVER);
            this.tablero.numplayers = remotePoints.getNumPlayers();
            System.out.println(Server.URL_SERVER);
            try{
                id = remotePoints.getId();
            }catch(Exception e){
                e.printStackTrace();
                //TODO if not id-> no puede jugar pq ya hay 5 jugadores
            }


            // Player Initial position
            // Handle score border
			boolean keepPlaying = true;
            while(keepPlaying){
				Random random = new Random();
			    int posx = random.nextInt(w - w/4)+ w/4;
			    int posy = random.nextInt(h);
			    System.out.println( posx  + " - " +  posy);
			    player = new Player(new Point(posx, posy),id);

			    int frames = 2;
			    int skipFrames = 0;

			    while(!remotePoints.allPlayersReady()){
			        continue;//sleep(100);
			    }

			    while (true) { // Main loop
					System.out.println("while true");	
			        // Controls
			        if(!player.ended){
			            if (keys[KeyEvent.VK_UP]) {
			                System.out.println("UP");
			                player.moveUp();
			            }
			            if (keys[KeyEvent.VK_DOWN]) {
			                System.out.println("DOWN");
			                player.moveDown();
			        	}

			    	}else{
						boolean allLost = remotePoints.allLost();
						if(allLost){
							System.out.println("allLost");
							break;	//break while true						
						}
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
					            // Returns the new point
					            new_point = player.growUp(true);
					            // Add the new point
					            remotePoints.addPoint(new_point, id);
							}
					        if(random.nextFloat()< 0.1){
					            skipFrames = 2 + random.nextInt(4);
					            System.out.println(skipFrames);
					        }
					    }

					    frames = 0;
					}

			    	// Tablero
			    	boolean aux=true;
					while(aux){
					    try {
					        // Pass the points to the board
					        tablero.points = remotePoints.getList();
					        // Obtaining the scores for drawing
					        tablero.scores = remotePoints.getScores();
					        player.score = remotePoints.getScore(player.id);

					        if(tablero.points[id].size()==0){
					            player.ended=true;
					        }
					        tablero.repaint();//paint the points in the board
					        aux=false;
					    }catch(java.rmi.UnmarshalException e){
					        //TODO print sometring??
					        System.out.println("wait");
					        continue;
			        	}
			    	}
					try {
					    this.sleep(1000 / UPDATE_RATE);
					} catch (InterruptedException ex) {

					}
				}//end while True
				//TODO press up to keep playing, down to stop playing
				
				tablero.repaint();//paint the points in the board
				while(true){//wainting for players to decide	
					System.out.println("waiting for key");			
					if (keys[KeyEvent.VK_UP]) {
					    System.out.println("UP");
						keepPlaying=true;
						break;
					}
					if (keys[KeyEvent.VK_DOWN]) {
						System.out.println("DOWN");
						keepPlaying=false;
						break;
					}
				}
				System.out.println("end");	

	    	}//end while keep playing
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

