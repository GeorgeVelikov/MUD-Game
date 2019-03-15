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


class ClientImplementation implements ClientInterface {
    // data connected to the server
    private ServerInterface remote;
    private String hostname;
    private int port;

    // game name
    private String mud_name;
    private Integer inventory_size;

    // user data
    private boolean playing;
    private String username;
    private String location;
    private List<String> inventory = new ArrayList<>();


    // setters
    private void setName(String name) {

        this.username = name.replace(" ", "");
    }

    private void setHostname(String name) {

        this.hostname = name;
    }

    private void setPort(int _port) {

        this.port = _port;
    }

    private void setLocation(String loc) {

        this.location = loc;
    }

    private void setInventorySize(Integer size) {
        /* Cleans up inventory once the users are prompted to the menu, I planned on server-wide inventories.
         * I would have treated MUDs like "zones" and servers as a "world", users can jump in and out of zones,
         * and find items planed through out the world. Honestly if this was a software project (making a game)
         * I would have went full blown and went for some sort of dungeon crawler (with graphics, of course).
        */
        this.inventory = new ArrayList<>();
        for(int i=0; i<size; i++) {
            this.inventory.add("[ ]");
        }
    }

    private void setMUDName(String mud_name) {

        this.mud_name = mud_name;
    }

    // server operations & game quitting/playing
    private void players() throws RemoteException {
        System.out.println(
                remote.playersOnline()
        );
    }

    private void menu() throws RemoteException {
        this.setInventorySize(this.inventory_size);

        while(!this.playing) {
            System.out.println(
                    this.remote.menu() + "\n"
            );

            String action = this.enterAction().toLowerCase();

            if (action.startsWith("create ")) {
                String game_name = action.replace("create ", "");

                if (this.remote.gameExists(game_name)) {
                    System.out.println("MUD " + game_name + " already exists"); }
                else {
                    System.out.println(this.remote.createMUDGameInstance(game_name)); }
            }

            if (action.startsWith("join ")) {
                String game_name = action.replace("join ", "");
                this.setMUDName(game_name);
                this.play();
            }

            if (action.equals("exit")) {
                this.disconnect(); break;
            }
        }
    }

    private void join() throws RemoteException {
        remote.playerJoin(this.username);
        System.out.println("\nWelcome to the MUD server " + this.hostname);
    }

    private void quit() throws RemoteException {
        this.remote.playerQuit(this.location, this.username, this.mud_name);
        this.playing = false;

        System.out.println("You have quit " + this.mud_name);
        this.mud_name = "";

        this.menu();
    }

    private void disconnect() {
        this.remote = null;
        this.hostname = null;
        this.port = 0; // null
    }
    // TODO: put name/port in connect so that we can choose which server to connect to
    private void connect() throws RemoteException {
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
                remote.playerMove(this.location, direction, this.username, this.mud_name)
        );

        System.out.print("Your new location is ");
        this.look();
    }

    private void look() throws RemoteException {
        String users = this.remote.playerLook(this.location);
        users = users.replaceAll("\\b"+this.username+"\\b", "<You>");

        System.out.println(
                "\nNode: " + this.location + users
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
                this.inventory + "\n"

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


    // game loop
    private void play() throws RemoteException {
        System.out.println("\nWelcome to MUD game " + this.mud_name);
        this.help();
        // game state vars
        this.playing = true;
        String action;

        // set starting loc
        this.setLocation(remote.playerStartLocation(this.username, this.mud_name));

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


    // input manager
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

    private String enterAction(String msg) {
        System.out.print(msg);

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        try {
            return input.readLine();
        }
        catch(IOException e) {
            return "";
        }
    }

    // creator
    ClientImplementation(String _hostname, int _port) throws RemoteException {
        System.setProperty("java.security.policy", ".policy");
        System.setSecurityManager(new SecurityManager());
        this.setHostname(_hostname);
        this.setPort(_port);

        this.connect(); // connects to the rmi server

        this.setName(this.enterAction("Enter your username: "));

        while (this.remote.playerExists(this.username)) {
            System.out.println("A user with the name " + this.username + " already exists in the server");
            this.setName(this.enterAction("Enter a different username: "));
        }

        this.join(); // actually join the connected server after basic verification

        this.inventory_size = 4;
        this.menu(); // loads up the game menu
    }
}
