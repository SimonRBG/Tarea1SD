package cl.uchile.dcc.cc5303;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;

public class Client extends Thread{
/**
 * Created by pecesito on 17-10-16.
 */

    public boolean[] keys;
    private final static String TITLE = "Juego - CC5303";
    public static IPoints remotePoints;
	public static IComm comm;

    private int w, h;
    private final static int UPDATE_RATE = 30;
    private final static int GROW_RATE = 3;
	private final static int margin_Border = 50;



    private JFrame frame;
    private Board tablero;
    private Player player;

    private int id;
    private String url_server, url_coordinator;

    public Client(String url_c) {
    	this.url_coordinator = url_c;
        //this.url_server=url_c;
        try {
	    w = Server.w;
	    h = Server.h;
            keys = new boolean[KeyEvent.KEY_LAST];
            frame = new JFrame(TITLE);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            tablero = new Board(w, h);

            frame.add(tablero);
            tablero.setSize(w, h);


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
            String ip = Util.getIp();
            System.out.println("my IP: "+ ip);
            String hostname = ip;
            System.setProperty("java.rmi.server.hostname", hostname);


			//recuperation of the coordinator Object(Comm)
			comm = (IComm) Naming.lookup(url_coordinator);
			while(!comm.getServer_ready()){
				//wait
				try {
					this.sleep(1000 / UPDATE_RATE);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			System.out.println("ServerReady");
			url_server= comm.getActual_url_server();
			// Recuperation of the shared object
            remotePoints = (IPoints) Naming.lookup(url_server);

			this.tablero.numplayers = remotePoints.getNumPlayers();
            System.out.println(url_server);
            try{
                id = remotePoints.getId();
            }catch(Exception e){
				System.out.println("Límite de jugadores Alcanzado!!");
				return;
            }
            // Player Initial position
            // Handle score border
			boolean keepPlaying = true;
            while(keepPlaying){
				Random random = new Random();
			    int posx = random.nextInt(w - margin_Border - w/4)+ w/4 + margin_Border;
			    int posy = random.nextInt(h);
			    System.out.println( posx  + " - " +  posy);
			    player = new Player(new Point(posx, posy),id);

			    int frames = 2;
			    int skipFrames = 0;
				//set me ready
				remotePoints.setReady(id,true,false);
				//wait for others
			    while(!remotePoints.allPlayersReady()){
			        continue;
			    }
				System.out.println("All Players Ready!");
				tablero.wait = false;
				// Main loop
			    while (true) {
			        // Controls
			        if(!player.ended){
						if (keys[KeyEvent.VK_UP]) {
							//System.out.println("UP");
							player.moveUp();
						}
						if (keys[KeyEvent.VK_DOWN]) {
							//System.out.println("DOWN");
							player.moveDown();
						}
						++frames;

						if (frames == GROW_RATE){
							Point new_point = null;
							if (skipFrames-- > 0){
								new_point = player.growUp(false);//returns the new point

							}else {
								skipFrames = 0;
								// Returns the new point
								new_point = player.growUp(true);
								if(random.nextFloat()< 0.1){
									skipFrames = 2 + random.nextInt(4);
								}
							}
							remotePoints.addPoint(new_point, id);
							frames = 0;
						}
					}

			    	// Tablero
					// Obtaining the scores for drawing
					tablero.scores = remotePoints.getScores();
					player.score = tablero.scores[player.id];
					// Pass the points to the board
					tablero.points = remotePoints.getList();
					player.ended = remotePoints.lost(id);

					tablero.repaint();//paint the points in the board=

					if(player.ended){
						if(remotePoints.allLost()){
							System.out.println("allLost");
							break;	//break while true
						}
						else{
							System.out.println("stillAlive"+id);
						}
					}

					try {
					    this.sleep(100 / UPDATE_RATE);
					} catch (InterruptedException ex) {
                        ex.printStackTrace();
					}
				}

				remotePoints.setReady(id,false,true);
                System.out.println("waiting for key: Y to continue, N to finish");
				tablero.points = null;
				tablero.press=true;
				tablero.repaint();//paint the points in the board

                while(true) {//wainting for players to decide
					if (keys[KeyEvent.VK_Y]) {
						System.out.println("Waiting for other players to answer");
						keepPlaying = true;
						remotePoints.setReady(id, true, false);
						break;
					}
					if (keys[KeyEvent.VK_N]) {
						System.out.println("bye-bye");
						keepPlaying = false;
						remotePoints.setReady(id, true, true);
						break;
					}
					try {
						this.sleep(1000 / UPDATE_RATE);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}

				tablero.press = false;
				tablero.wait = true;
				tablero.repaint();//paint the points in the board
	    	}

            //end while keep playing
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
		tablero.wait = false;
		tablero.bye = true;
		tablero.repaint();//paint the points in the board
		try {
			this.sleep(1000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
        try {
        	//Close game
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            this.finalize();
        }catch(java.lang.Throwable e){
            e.printStackTrace();
        }
    }
}

