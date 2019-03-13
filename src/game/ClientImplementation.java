package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientImplementation implements ClientInterface {
    // server connected to do data
    private ServerInterface remote;
    private String hostname;
    private int port;

    // user data
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

    // server operations
    public void checkClients() throws RemoteException {
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
        boolean playing = true;
        String action;

        while(playing) {
            this.location = this.remote.playerStartLocation();

            action = enterAction();

            if (action.equals("quit"))      { playing = false; this.quit(); }
            if (action.equals("players"))   { this.checkClients(); }
            if (action.equals("loc")) { System.out.println(this.location); }
        }
    }

    public void move(String direction) throws RemoteException {

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
