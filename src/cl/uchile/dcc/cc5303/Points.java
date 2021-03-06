package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 12-10-16.
 */

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static java.lang.Math.abs;

public class Points extends UnicastRemoteObject  implements IPoints, Serializable {

    public int numplayers;
    public Stack ids = new Stack();
    public LinkedHashSet<Point> list[];// We have to keep in mind the order
    public int scores[];
    public boolean looses[];
    public boolean allLost;
    public boolean ready[];
    public boolean waitingEnd[];
    public int w, h;
    public HashMap<Integer, Integer> updateValue;
    public boolean waiting = false;
    boolean someOneQuit;
    boolean gamePaused = false;
    public boolean someOneWaiting = false;

    public Object mutex2 = new Object();

    public Points(int n, int w, int h) throws RemoteException{
        synchronized (mutex2) {
            this.w = w;
            this.h = h;
            this.waiting = false;
            this.someOneWaiting = false;
            this.someOneQuit = false;
            this.scores = new int[n];
            this.looses = new boolean[n];
            this.ready = new boolean[n];
            this.allLost = false;
            this.numplayers = n;
            this.updateValue = new HashMap<Integer, Integer>();
            list = new LinkedHashSet[n];
            for (int i = 0; i < n; i++) {
                this.looses[i] = false;
                this.ready[i] = false;
                this.scores[i] = -1;
                list[i] = new LinkedHashSet<Point>();
                ids.push(n - 1 - i);
            }
        }
    }

    public void SetPoints(int[] scores, boolean[] looses, boolean allLost, boolean[] ready,LinkedHashSet<Point>[] l, Stack ids, int numplayers, boolean someOneQuit, boolean waiting, boolean someOneWaiting, HashMap<Integer, Integer> updateValue, boolean gamePaused ) throws RemoteException{
        synchronized (mutex2) {
            this.allLost = allLost;
            this.list = l;
            this.scores = scores;
            this.looses = looses;
            this.ready = ready;
            this.ids = ids;
            this.numplayers = numplayers;
            this.someOneQuit = someOneQuit;
            this.updateValue = updateValue;
            this.waiting = waiting;
            this.someOneWaiting = someOneWaiting;
            this.gamePaused = gamePaused;

        }
    }

    public Points getPoints() throws RemoteException{
        synchronized (mutex2) {
            return this;
        }

    }

    public void setUpdateValue(int ind) throws RemoteException {
        synchronized (mutex2) {
            if (!updateValue.containsKey(ind))
                this.updateValue.put(ind, 0);
            updateValue.put(ind, updateValue.get(ind) + 1);
        }
    }

    public int getUpdateValue(int ind) throws RemoteException {
        synchronized (mutex2) {
            if (!updateValue.containsKey(ind))
                this.updateValue.put(ind, 0);
            return updateValue.get(ind);
        }
    }

    public boolean addPoint(Point po, int i) throws RemoteException{
        try {
            if (po == null)
                return false;
            // If the point doesn't choc with another one
            synchronized (mutex2) {
                if (check(po)) {
                    list[i].add(po);
                    looses[i]=false;
                    notifyOperation("new Point Added" + po.getX() + ", " + po.getY() + ", " + po.getVisible() + ". id: " + i);
                    return true;
                } else {
                    // Clear all the snake, save tha it lost and telling to the other to update their score
                    list[i].clear();
                    this.looses[i] = true;
                    notify_score(i);
                    notifyOperation("player " + i + " lost!!");
                    if (checkAllPlayers()) {
                        this.allLost = true;
                        notifyOperation("all Lostttt");
                        //TODO borrar todo y empezar de nuevo
                    }
                    return false;
                }
            }


        }catch(NullPointerException e){
            System.out.println(list);
            System.out.println(list[i]);
            System.out.println("NPE en Points");
            e.printStackTrace();
            return false;
        }
    }
    public void setWaitingResponse(boolean val) throws RemoteException {
        synchronized (mutex2) {
            this.waiting = val;
        }
    }

    public boolean getWaitingResponse() throws RemoteException {
        synchronized (mutex2) {
            return waiting;
        }
    }


    public boolean lost(int id)throws RemoteException{
        synchronized (mutex2) {
            return looses[id];
        }

    }

    public boolean allLost() throws RemoteException{
        synchronized (mutex2) {
            if(allLost){
                for(int j = 0; j< numplayers;j++){
                    list[j].clear();
                }
            }
            return this.allLost;
        }

    }

    private boolean checkAllPlayers(){
        synchronized (mutex2) {
            for (int i = 0; i < this.numplayers; i++) {
                if (!this.looses[i]){
                    return false;
                }
            }
            return true;
        }
    }

    private boolean check(Point p) throws RemoteException {
            for (int i = 0; i < this.numplayers; i++) {
                // TODO : Change the evaluation criteria
                Iterator it = ((LinkedHashSet) list[i].clone()).iterator();
                int px = p.getX();
                int py = p.getY();
                boolean pv = p.getVisible();
                // Check border
                // Handle the score boards
                if (px > w || px < w / 4 || py > h || py < 0) {
                    return false;
                }
                //Check other points
                while (it.hasNext()) {
                    //try {
                        Point p2 = (Point)it.next();
                        if (abs(px - p2.getX()) < Point.dHip / 3 * 2 && abs(py - p2.getY()) < Point.dHip / 3 * 2 && (p2.getVisible() == pv && pv)) {
                            return false;
                        }
                    /*} catch (ConnectException e) {
                        // Free a space for new player
                        System.out.println("check and quit!!!!!!!!!!!!!");
                        this.setQuit(i);
                        // Don't draw the player that we can't connect
                        break;
                    }*/
                    it.remove();
                }
            }
            return true;
    }

    public LinkedHashSet<Point>[] getList() throws RemoteException {
        synchronized (mutex2) {
            return list.clone();
        }
    }

    private void notify_score(int id) {
        synchronized (mutex2) {
            // Update the score of all the others snakes alive
            for (int i = 0; i < numplayers; i++) {
                if (i != id && !this.looses[i])
                    scores[i]++;
            }
        }
    }

    public int getScore(int id) throws RemoteException {
        synchronized (mutex2) {
            return scores[id];
        }
    }

    public int[] getScores() throws RemoteException {
        synchronized (mutex2) {
            return scores;
        }

    }

    public int getNumPlayers() throws RemoteException {
        synchronized (mutex2) {
            return numplayers;
        }
    }

    // Gave a new id for 5 users
    public int getId() throws RemoteException{
        synchronized (mutex2) {
            int id = (int)ids.pop();
            notifyOperation("new id " + id);
            scores[id] = 0;
            this.ready[id] = true;
            this.looses[id] = false;
            System.out.println("ids:" + ids);
            return id;

        }
    }

    public int getId(int id) throws RemoteException{
        synchronized (mutex2) {
            try{
                ids.remove(id);
                notifyOperation("new id " + id);
                //TODO recover variables from points(?)
                //scores[id] = 0;
                this.ready[id] = true;
                if(list[id].size()>0){
                    this.looses[id]=false;
                }else{//TODO fix
                    this.looses[id]=true;
                }
                //this.looses[id] = false;
                //System.out.println("ids:" + ids);
                return id;
            }catch(ArrayIndexOutOfBoundsException e){
                System.out.println("Id already taken: "+ id);
                return getId();
            }
        }
    }


    public boolean allPlayersReady() throws RemoteException{
        synchronized (mutex2) {
            for (int i = 0; i < numplayers; i++) {
                if (!ready[i])
                    return false;
            }
            return true;
        }
    }

    public void setReady(int id, boolean r, boolean l) throws RemoteException{
        synchronized (mutex2) {
            ready[id] = r;
            looses[id] = l;
            if (!l) {
                allLost = false;
            }
        }
    }

    public boolean[] getLooses() throws RemoteException{
        synchronized (mutex2){
            return this.looses;
        }
    }

    public void notLost(int id) throws RemoteException{
        synchronized (mutex2){
            looses[id]=false;
        }
    }

    public void setQuit(int id, boolean voluntary) throws  RemoteException{
        synchronized (mutex2) {
            if(voluntary){
                list[id].clear();
                this.scores[id] = -1;
                this.ready[id] = true;
                this.someOneQuit = true;
                notify_score(id);
            }
            this.looses[id] = true;


            if (checkAllPlayers()) {
                this.allLost = true;
                for(int i = 0; i< numplayers;i++){
                    list[i].clear();
                }
                notifyOperation("all Lost");
            }
            // Id available & reinitilize the score
            if(!ids.contains(new Integer(id))) {
                ids.push(new Integer(id));
            }


            notifyOperation("player " + id + " quit!!");
        }
    }

    public boolean someOneQuit() throws  RemoteException{
        synchronized (mutex2) {
            if (someOneQuit) {
                someOneQuit = false;
                return true;
            } else {
                return false;
            }
        }
    }

    public void setSomeOneWaiting(boolean value) throws RemoteException{
        synchronized (mutex2) {
            this.someOneWaiting = value;
        }
    }

    public boolean getSomeOneWaiiting() throws RemoteException{
        synchronized (mutex2) {
            return someOneWaiting;
        }
    }
    public boolean gamePaused() throws RemoteException {
        synchronized (mutex2) {
            return gamePaused;
        }
    }

    public boolean setPause() throws RemoteException {
        synchronized (mutex2) {
            this.gamePaused = !this.gamePaused;
            return this.gamePaused;
        }
    }


    private void notifyOperation(String s){
        System.out.println("Operation: "+s);
    }


    public int compareTo(Points p ) {
        return this.toString().compareTo(p.toString());
    }

    @Override
    public String toString() {
        synchronized (mutex2) {
            StringBuffer sb = new StringBuffer("SomeOneQuit:").append(this.someOneQuit ? 1 : 0).append(";");
            sb.append("NumPlayers:").append(numplayers).append(";");

            sb.append("Scores:");

            for (int i = 0; i < numplayers; i++) {
                sb.append(this.scores[i]);
                sb.append(" ");
            }
            sb.append(";");

            sb.append("Looses:");
            for (int i = 0; i < numplayers; i++) {
                sb.append(this.looses[i] ? 1 : 0);
                sb.append(" ");
            }
            sb.append(";");

            sb.append("Ready:");
            sb.append("");
            for (int i = 0; i < numplayers; i++) {
                sb.append(this.ready[i] ? 1 : 0);
                sb.append(" ");
            }
            sb.append(";");

            sb.append("AllLost:").append(allLost ? 1 : 0).append(";");


            sb.append("Ids:");
            System.out.println("ids:"+this.ids);
            for (int i = 0; i < this.ids.size(); i++) {
                sb.append(this.ids.get(i)).append(" ");
            }
            sb.append(";");

            sb.append("List:");
            for (int i = 0; i < numplayers; i++) {
                try {
                    Iterator it = list[i].iterator();
                    while (it.hasNext()) {
                        try {
                            Point p = (Point) it.next();
                            sb.append("Point").append(p.toString());
                        } catch (NullPointerException e) {
                            System.out.println("no Point found");
                        }
                    }
                }catch(NullPointerException | ArrayIndexOutOfBoundsException e){
                    System.out.println("no list for player" + i);
                }
                sb.append(",");
            }
            sb.append(";");

            String suv = updateValue.toString();
            sb.append(suv.substring(1,suv.length()-1));


            sb.append(";");
            String swaiting = waiting?"1":"0";
            sb.append("Waiting:").append(swaiting);

            sb.append(";");
            String ssomeOneWaiting = someOneWaiting?"1":"0";
            sb.append("SomeOneWaiting:").append(ssomeOneWaiting);

            sb.append(";");
            String sgamePaused = gamePaused?"1":"0";
            sb.append("GamePaused:").append(sgamePaused);


            //sb.append(";");




            return sb.toString();
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException{
        //perform the default serialization for all non-transient, non-static fields
        out.defaultWriteObject();

    }

    public Points(String s, int w, int h)  throws RemoteException {


        synchronized (mutex2) {
            this.w = w;
            this.h = h;
            String[] sa = s.split(";");
            //SomeOneQuit:0; NumPlayers:2; Scores:[-1 -1 ]; Looses:[0 0 ]; Ready:[0 0 ]; AllLost:0; Ids:[1, 0]; List:[][];
            try {
                someOneQuit = (Integer.parseInt(sa[0].split(":")[1]) == 0) ? false : true;
                System.out.println("someOneQuit: " + someOneQuit);

                numplayers = Integer.parseInt(sa[1].split(":")[1]);
                System.out.println("numPlayers: "+ numplayers);


                String sscores = sa[2].split(":")[1];
                String[] ssscores = sscores.split(" ");
                //System.out.println(sscores);
                scores = new int[numplayers];
                System.out.print("scores: ");
                for (int i = 0; i < numplayers; i++) {
                    scores[i] = Integer.parseInt(ssscores[i]);
                    System.out.print(scores[i]);
                }
                System.out.println();
                String slooses = sa[3].split(":")[1];
                String[] sslooses = slooses.split(" ");
                looses = new boolean[numplayers];
                System.out.print("looses: ");
                for (int i = 0; i < numplayers; i++) {
                    looses[i] = (Integer.parseInt(sslooses[i]) == 0) ? false : true;
                    System.out.print(looses[i]+" ");
                }
                System.out.println();


                String sready = sa[4].split(":")[1];
                String[] ssready = sready.split(" ");
                ready = new boolean[numplayers];
                System.out.print("ready: ");
                for (int i = 0; i < numplayers; i++) {
                    ready[i] = (Integer.parseInt(ssready[i]) == 0) ? false : true;
                    System.out.print(ready[i]+" ");
                }
                System.out.println();


                allLost = (Integer.parseInt(sa[5].split(":")[1]) == 0) ? false : true;
                System.out.println("allLost: "+allLost);


                this.ids = new Stack();
                String sids;
                System.out.print("ids:");
                try {
                    sids = sa[6].split(":")[1];
                    String[] ssids = sids.split(" ");

                    for (int i = 0; i < ssids.length; i++) {
                        ids.push(Integer.parseInt(ssids[i]));
                        System.out.print(" " + ssids[i]);
                    }

                    //System.out.println("ids:"+ids.toString());
                /*for (int i = ssids.length-1; i >= 0; i--) {
                    ids.push(ssids[i]);
                }*/
                }catch (ArrayIndexOutOfBoundsException e){
                    System.out.print( "no ids");
                }
                System.out.println();
                //System.out.println(sids);
                //ids.clear();




                String slist;
                try {
                    slist = sa[7].split(":")[1];
                }catch(ArrayIndexOutOfBoundsException e){
                    slist = "";
                }
                String[] sslist = slist.split(",");
                list = null;
                this.list = new LinkedHashSet[numplayers];

                for (int i = 0; i < numplayers; i++) {
                    list[i] = new LinkedHashSet<Point>();
                    try{
                        String[] ssslist = sslist[i].split("Point");
                        System.out.println("Points player "+ i);
                        for (int j = 1; j < ssslist.length; j++) {
                            if(ssslist[j]!=" ") {
                                Point p = new Point(ssslist[j]);
                                if (p != null) {
                                    list[i].add(p);
                                    System.out.print("Point: " + p.toString() + " ");
                                } else {
                                    System.out.print("null point:" + ssslist[j] + " ");
                                }
                            }
                        }
                    }catch (NullPointerException e){
                        System.out.println("no list found for player "+ i);
                    }catch(ArrayIndexOutOfBoundsException e){
                        System.out.println("no list for player "+ i);
                    }
                }

                this.updateValue = new HashMap<Integer, Integer>();
                String sUpdateValue = sa[8];
                String[] ssuv = sUpdateValue.split(",");
                for(int i = 0; i<ssuv.length; i++){
                    String[] sssuv = ssuv[i].split("=");
                    try {
                        Integer k = new Integer(Integer.parseInt(sssuv[0].trim()));
                        Integer v = new Integer(Integer.parseInt(sssuv[1].trim()));
                        updateValue.put(k, v);
                    }catch(NumberFormatException e){
                        System.out.println("k,v number format Exception: "+ sUpdateValue);
                    }
                }

                System.out.println("UV: "+ updateValue);

                waiting = (Integer.parseInt(sa[9].split(":")[1]) == 0) ? false : true;
                System.out.println("waiting: "+waiting);

                someOneWaiting = (Integer.parseInt(sa[10].split(":")[1]) == 0) ? false : true;
                System.out.println("someoneWaiting:"+someOneWaiting);

                gamePaused = (Integer.parseInt(sa[11].split(":")[1]) == 0) ? false : true;
                System.out.println("GamePaused:"+gamePaused);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
