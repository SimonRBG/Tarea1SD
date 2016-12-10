package cl.uchile.dcc.cc5303;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.EmptyStackException;
import java.util.LinkedHashSet;
import java.util.Random;
import java.rmi.ConnectIOException;

public class Client extends Thread{
/**
 * Created by pecesito on 17-10-16.
 */

    public boolean[] keys;
    private final static String TITLE = "Juego - CC5303";
    public static IPoints remotePoints;
	public static IComm comm;

    private int w, h;
    private final static int UPDATE_RATE = 5;
    private final static int GROW_RATE = 3;
	private final static int margin_Border = 50;



    private JFrame frame;
    private Board tablero;
    private Player player;

    private int id;
    private String url_server, url_coordinator;

    public Client(String url_c) {
    	this.url_coordinator = url_c;
        this.url_server=url_c;
        try {
	    w = Server.w;
	    h = Server.h;
            keys = new boolean[KeyEvent.KEY_LAST];
            frame = new JFrame(TITLE);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            synchronized (comm.mutex) {
                tablero = new Board(w, h, Point.dHip);
            }
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
		String ip = Util.getIp();
		System.out.println("my IP: "+ ip);
		String hostname = ip;
		System.setProperty("java.rmi.server.hostname", hostname);
    }

	public void sleep(){
		try {
			this.sleep(1000 / UPDATE_RATE);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public void waitRecuperation(){

		tablero.serverDown = true;
		boolean serverRecup = false;
		while (!serverRecup) {
			// TODO : Verify with the function if the server is recuperating
			this.sleep();
			tablero.paint(tablero.getGraphics());
			tablero.p = (tablero.p+1)%3;
			try {
				String newUrl = comm.getActual_url_server();
				url_server = newUrl;
			}catch(RemoteException e){
				System.out.println("Unable to connect to Coordinator");
				continue;
			}
			try {
				remotePoints = (IPoints) Naming.lookup(url_server);
				try {
					if (comm.getServer_ready()) {
						serverRecup = true;
						tablero.serverDown = false;
						loadData();
						// TODO : do the treatment to recupare all points
					}
				}catch(RemoteException e){
					System.out.println("Unable to connect to Coordinator");
					continue;
				}
			}catch( NotBoundException | MalformedURLException| RemoteException e){
				continue;
			}
		}
		tablero.paint(tablero.getGraphics());
		System.out.println();

	}

    public void checkMigration()throws ConnectException ,java.rmi.UnmarshalException{
		try {
			String newUrl = comm.getActual_url_server();
			if (!url_server.contentEquals(newUrl)) {
				System.out.println("Url Changed: "+ url_server +"->" + newUrl);
				url_server = newUrl;
				try {
					remotePoints = (IPoints) Naming.lookup(url_server);
				} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
					throw e;
				}
			}
		}catch (RemoteException | NotBoundException | MalformedURLException e){
			e.printStackTrace();
		}  catch (ClassCastException e) {
			System.err.println("No server found");
			System.exit(1);
		}
	}

	public void loadData(){
		try {
			LinkedHashSet<Point>[] points = remotePoints.getList();
			tablero.points = points;
			tablero.scores = remotePoints.getScores();
			allLost = remotePoints.allLost();
			keepPlaying = true;

			if(player!=null){
				player.ended = remotePoints.lost(id);
				System.out.println("ended:"+player.ended);
				try {
					player.head = (Point)points[id].toArray()[points[id].size()-1];
					System.out.println("head: "+ player.head);

				}catch(Exception e){
					System.out.println("no points");
				}
			}
		}catch(RemoteException e){
			waitRecuperation();
		}
	}

	boolean allLost = false;
	boolean keepPlaying = true;
	Random random;
    @Override
    public void run() {
        try {

			//recuperation of the coordinator Object(Comm)
			comm = (IComm) Naming.lookup(url_coordinator);
			while(!comm.getServer_ready()){
				//wait
				System.out.println("waiting Server");
				try {
					this.sleep(1000 / UPDATE_RATE);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			System.out.println("ServerReady");

			synchronized (comm.mutex) {
				try {
					checkMigration();
					// Recuperation of the shared object
					remotePoints = (IPoints) Naming.lookup(url_server);
				} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
					this.waitRecuperation();
					return;

				}
			}

			synchronized (comm.mutex) {

				try {
					checkMigration();
					this.tablero.numplayers = remotePoints.getNumPlayers();
				} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
					this.waitRecuperation();
					return;
					//TODO check this return
				}
			}

			System.out.println(url_server);

			try{
				synchronized (comm.mutex) {
                    checkMigration();
					try {
						id = remotePoints.getId();
						tablero.id = id;
						System.out.println("ID: " + id);
					} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
						this.waitRecuperation();
						return;
						//TODO check this return
					}
				}
            }catch(EmptyStackException e){
				System.out.println("Límite de jugadores Alcanzado!!");
				try {
					this.sleep(3);
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
					this.finalize();
				}catch (Throwable ex){
					ex.printStackTrace();
				}
				return;
				//TODO check this return
			} catch (RemoteException e) {
				e.printStackTrace();
			}
            // Player Initial position
            // Handle score border

			kp:
            while(keepPlaying){
				int frames = 2;
				int skipFrames = 0;
				if (player == null) {
					random = new Random();
					int posx = random.nextInt(w - margin_Border - w / 4) + w / 4 + margin_Border;
					int posy = random.nextInt(h);
					System.out.println(posx + " - " + posy);
					player = new Player(new Point(posx, posy), id);

					//set me ready
					synchronized (comm.mutex) {

						try {
							checkMigration();
							remotePoints.setReady(id, true, false);
						} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
							this.waitRecuperation();
							continue kp;
						}
					}
				}

				//wait for others
				boolean allready = false;
				while(!allready){
					synchronized (comm.mutex) {
						checkMigration();
						try {
							remotePoints.setUpdateValue(id);
							allready = remotePoints.allPlayersReady();
						} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
							this.waitRecuperation();
							continue kp;
						}
					}
					this.sleep();
			    }
			    synchronized (comm.mutex) {
					checkMigration();
					try {
						remotePoints.setWaitingResponse(false);
					} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
						this.waitRecuperation();
						continue kp;
					}

				}
				System.out.println("All Players Ready!");
				tablero.wait = false;
				// Main loop

				synchronized (comm.mutex) {
					checkMigration();
					try {
						allLost = remotePoints.allLost();
						System.out.println("allLost ="+allLost );
					} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
						this.waitRecuperation();
						continue kp;
					}
				}
			    while (!allLost) {
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
						if (keys[KeyEvent.VK_Q]) {
							// We quit the game
							System.out.println("bye-bye");
							synchronized (comm.mutex){
								checkMigration();
								try {
									//System.out.println("client press Q 1");
									remotePoints.setQuit(id);
								} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
									this.waitRecuperation();
									continue kp;
								}
							}
							synchronized (comm.mutex) {
								checkMigration();
								try {
									//player.ended = remotePoints.lost(id);
									boolean ready=true;
									remotePoints.setReady(id, ready, true);
								} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
									this.waitRecuperation();
									continue kp;
								}
							}
							keepPlaying = false;
							break kp;
							//System.exit(0);
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

							synchronized (comm.mutex) {
                                checkMigration();
								try {
									remotePoints.addPoint(new_point, id);
								} catch (ConnectException | java.rmi.UnmarshalException | ConnectIOException e){
									this.waitRecuperation();
									continue kp;
								}
							}
							frames = 0;
						}
					}

			    	// Tablero
					// Obtaining the scores for drawing
					//waitMigrating();

					synchronized (comm.mutex) {
                        checkMigration();
						try {
							tablero.scores = remotePoints.getScores();
						}catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
							this.waitRecuperation();
							continue kp;
						}
					}
					player.score = tablero.scores[id];
					// Pass the points to the board
					//waitMigrating();

					synchronized (comm.mutex) {
                        checkMigration();
						try {
							tablero.points = remotePoints.getList();
						} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
							this.waitRecuperation();
							continue kp;
						}
					}
					//waitMigrating();

					synchronized (comm.mutex) {
                        checkMigration();
						try {
							player.ended = remotePoints.lost(id);
							System.out.println("player Ended: "+ remotePoints.lost(id));
						} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
							this.waitRecuperation();
							continue kp;
						}
					}
                    synchronized (comm.mutex) {
                        tablero.repaint();//paint the points in the board=
                    }

					if(player.ended){
						//waitMigrating();

						synchronized (comm.mutex) {
                            checkMigration();
							try {
								allLost = remotePoints.allLost();
							} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
								this.waitRecuperation();
								continue kp;
							}
						}
						if(allLost){
							synchronized (comm.mutex) {
								checkMigration();
								try {
									remotePoints.setSomeOneWaiting(false);
								} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
									this.waitRecuperation();
									continue kp;
								}

							}
							System.out.println("allLost");
							break;	//break while true
						}
						else{
							System.out.println("stillAlive"+id);
							synchronized (comm.mutex) {
								checkMigration();
								try {
									remotePoints.setSomeOneWaiting(true);
								} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
									this.waitRecuperation();
									continue kp;
								}

							}
						}
					}

					try {
					    this.sleep(100 / UPDATE_RATE);
					} catch (InterruptedException ex) {
                        ex.printStackTrace();
					}
				}
				allLost = false;
				//waitMigrating();

				synchronized (comm.mutex) {
                    checkMigration();
					try {
						remotePoints.setReady(id, false, true);
					} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
						this.waitRecuperation();
						continue kp;
					}
				}
                System.out.println("waiting for key: Y to continue, Q to Quit");
				tablero.points = null;
				tablero.press=true;
				tablero.repaint();//paint the points in the board

				boolean ready = false;
				ready:
                while(!ready){
                	//wainting for players to decide
					// Verify that it doesn't die
					synchronized (comm.mutex) {
						checkMigration();
						try {
							remotePoints.setUpdateValue(id);
							remotePoints.setWaitingResponse(true);
						} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e) {
							this.waitRecuperation();
							continue ready;
						}
					}
					if (keys[KeyEvent.VK_Y]){
						System.out.println("Waiting for other players to answer");
						keepPlaying = true;
						synchronized (comm.mutex) {
                            checkMigration();
							try {
								ready=true;
								remotePoints.setReady(id, ready, false);
							} catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
								this.waitRecuperation();
								continue ready;
							}
						}
						break;
					}
					if (keys[KeyEvent.VK_Q]) {
						System.out.println("bye-bye");
						keepPlaying = false;
						synchronized (comm.mutex){
							checkMigration();
							try {
								//ready=true;
								//System.out.println("Client press q 2");
								remotePoints.setQuit(id);
							}
							catch (ConnectException | ConnectIOException | java.rmi.UnmarshalException e){
								this.waitRecuperation();
								continue ready;
							}
						}
						break;
					}
					try {
						this.sleep(1000/UPDATE_RATE);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
				player = null;
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

