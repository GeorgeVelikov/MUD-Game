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
    private String mud_name;
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

    private void setInventorySize(Integer size) {
        for(int i=0; i<size; i++) {
            this.inventory.add("[ ]");
        }
    }

    // server operations & game quitting/playing
    public void players() throws RemoteException {
        System.out.println(
                remote.playersOnline()
        );
    }

    public void menu() throws RemoteException {
        while(!this.playing) {
            System.out.println(
                    this.remote.menu() + "\n"
            );

            String action = this.enterAction().toLowerCase();

            if (action.startsWith("create ")) {
                this.remote.createMUD(action.replace("create ", ""));
            }

            if (action.startsWith("join ")) {
                System.out.println(action.replace("join ", ""));
            }

            if (action.equals("exit")) {
                this.disconnect(); break;
            }
        }
    }

    public void join() throws RemoteException {
        remote.playerJoin(this.username);
        System.out.println("\nWelcome to the MUD server " + this.hostname);
    }

    public void quit() throws RemoteException {
        remote.playerQuit(this.username);
        this.playing = false;

        System.out.println("You have quit " + this.mud_name);
        this.mud_name = "";

        this.menu();
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
                        this.remote.playerLook(this.location).replace(this.username, "<You>")
        );
    }

    private void take(String item) throws RemoteException {
        boolean item_exists = this.remote.playerTake(this.location, item);

        if(item_exists) {
            for(int i=0; i<=this.inventory.size(); i++){
                if (this.inventory.get(i).equals("[ ]")) {
                    this.inventory.set(i, "[" + item + "]");
                    System.out.println("You have added " + item + " to your inventory");
                    this.checkInventory();
                    break;
                }

            }
        }


        else
            System.out.println("I cannot find this item");

    }

    private void checkInventory() {
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
        this.setLocation(remote.playerStartLocation(this.username));

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
        this.setInventorySize(4);

        System.setProperty("java.security.policy", ".policy");
        System.setSecurityManager(new SecurityManager());
    }
}
