package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


public class ClientImplementation implements ClientInterface {
    // server connected to do data
    private ServerInterface remote;
    private String hostname;
    private int port;

    // user data
    private boolean playing;
    private String username;
    private String location;
    private List<String> inventory = new ArrayList<>();


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


    // game helpers & prompts for server to calculate user actions
    private void move(String direction) throws RemoteException {
        this.setLocation(
                remote.playerMove(this.location, direction, this.username)
        );

        System.out.print("Your new location is ");
        this.look();
    }

    private void look() throws RemoteException {
        System.out.println(
                "Node: " + this.location +
                        this.remote.playerLook(this.location)
        );
    }

    private void take(String item) throws RemoteException {
        boolean item_exists = this.remote.playerTake(this.location, item);

        if(item_exists) {
            this.inventory.add(item);
            System.out.println("You have added " + item + " to your inventory");
            this.checkInventory();
        }

        else
            System.out.println("I cannot find this item");

    }

    private void checkInventory() throws RemoteException {
        System.out.println(
                "Your inventory: " +
                this.inventory
        );
    }

    private void help() {
        System.out.println(
                "\t~-~-~-~-~-~-~-~-~-~> HELP <~-~-~-~-~-~-~-~-~-\n" +
                "\t\t\t   SERVER: \tquit, players\n" +
                "\tPLAYER OPERATIONS: \tlook, take\n" +
                "\t\t\t MOVEMENT: \tnorth, west, south, east\n" +
                "\t~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~\n"
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


    // game loop
    public void play() throws RemoteException {
        // game state vars
        this.playing = true;
        String action;

        // set starting loc
        this.setLocation(remote.playerStartLocation());

        while(this.playing) {
            // sanitize action text
            action = enterAction().toLowerCase();

            if (action.equals("help")) {
                this.help(); }
            if (action.equals("quit")) {
                this.quit(); }
            if (action.equals("players")) {
                this.players(); }
            if (action.equals("look")) {
                this.look(); }
            if (action.equals("inventory")) {
                this.checkInventory(); }

            if (action.startsWith("take ")) {
                this.take(action.replace("take ", "")); }

            if (action.matches("north|west|south|east")) {
                this.move(action); }
        }
    }


    // creator
    ClientImplementation(String _hostname, int _port, String _username) {
        this.setHostname(_hostname);
        this.setPort(_port);
        this.setName(_username);

        System.setProperty("java.security.policy", ".policy");
        System.setSecurityManager(new SecurityManager());
    }
}
