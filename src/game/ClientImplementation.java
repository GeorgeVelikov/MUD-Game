package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

public class ClientImplementation implements ClientInterface {
    // server connected to do data
    private ServerInterface remote;
    private String hostname;
    private int port;

    // user data
    private boolean playing;
    private String username;
    private String location;


    // setters
    public void setName(String name) {

        this.username = name;
    }

    public void setHostname(String name) {

        this.hostname = name;
    }

    public void setPort(int _port) {

        this.port = _port;
    }

    private void setLocation(String loc) {

        this.location = loc;
    }


    // server operations
    public void players() throws RemoteException {
        System.out.println(
                remote.playersOnline()
        );
    }

    public void join() throws RemoteException {
        System.out.println(
                remote.playerJoin(this.username)
        );
    }

    public void quit() throws RemoteException {
        this.playing = false;

        System.out.println(
                remote.playerQuit(this.username)
        );

        this.disconnect();
    }

    public void disconnect() {
        this.remote = null;
        this.hostname = null;
        this.port = 0; // null
    }
    // TODO: put name/port in connect so that we can choose which server to connect to
    public void connect() throws RemoteException {
        try {
            String url = "rmi://" + this.hostname + ":" + this.port + "/mud";
            this.remote = (ServerInterface) Naming.lookup(url);
        }
        catch(NotBoundException e) {
            System.err.println("Error, Server not bound: " + e.getMessage());
        }
        catch(MalformedURLException e) {
            System.err.println("Error, Malformed url: " + e.getMessage());
        }
    }


    // game
    public void play() throws RemoteException {
        // game state vars
        this.playing = true;
        String action;

        // set starting loc
        this.setLocation(remote.playerStartLocation());

        while(this.playing) {
            // sanitize action text
            action = enterAction().toLowerCase();

            if (action.equals("quit")) {
                this.quit(); }
            if (action.equals("players")) {
                this.players(); }
            if (action.equals("look")) {
                this.look(); }
            if (action.matches("north|west|south|east")) {
                this.move(action); }
        }
    }

    private void move(String direction) throws RemoteException {

    }

    private void look() throws RemoteException {
        System.out.println(
                "\nNode: " + this.location +
                this.remote.playerLook(this.location)
        );
    }

    private String enterAction() {
        System.out.print(this.username + "~> ");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        try {
            return input.readLine();
        }
        catch(IOException e) {
            return enterAction();
        }
    }

    ClientImplementation(String _hostname, int _port, String _username) {
        this.hostname = _hostname;
        this.port = _port;
        setName(_username);

        System.setProperty("java.security.policy", ".policy");
        System.setSecurityManager(new SecurityManager());
    }
}
