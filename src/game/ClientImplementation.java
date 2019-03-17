package game;

import java.net.MalformedURLException;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.Naming;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;


class ClientImplementation implements ClientInterface {
    // data connected to the server
    private ServerInterface remote;
    private String hostname;
    private int port;

    // game name
    private String mud_name = "";

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

    private void setInventorySize() throws RemoteException {
        /* Cleans up inventory once the users are prompted to the menu, I planned on server-wide inventories.
         * I would have treated MUDs like "zones" and servers as a "world", users can jump in and out of zones,
         * and find items planed through out the world. Honestly if this was a software project (making a game)
         * I would have went full blown and went for some sort of dungeon crawler (with graphics, of course).
         */
        this.inventory = this.remote.setPlayerInventory();
    }

    private void setMUDName(String mud_name) {

        this.mud_name = mud_name;
    }


    // menu
    private void menu() throws RemoteException {
        try {
            this.setInventorySize();

            try {
                while (!this.playing) {
                    System.out.println(
                            this.remote.menu()
                    );

                    String action = this.enterAction().toLowerCase().trim();

                    if (action.startsWith("create ")) {
                        this.createMUD(action);
                    } else if (action.startsWith("join ")) {
                        String game_name = action.replace("join ", "");
                        this.joinMUDGame(game_name);
                    } else if (action.equals("disconnect")) {
                        this.disconnectServer();
                    } else {
                        System.out.println("Error, command " + action + " does not exist");
                    }
                }
            }
            catch(ConnectException e) { this.abort(); }
        }
        catch (NullPointerException e) { assert true; /* do nothing */ }
    }


    // server verification/join/status/quit
    private void connectServer() throws RemoteException {
        try {
            String url = "rmi://" + this.hostname + ":" + this.port + "/mud";
            this.remote = (ServerInterface) Naming.lookup(url);
        } catch (NotBoundException e) {
            System.out.println("Error, Server not bound: " + e.getMessage());
        } catch (MalformedURLException e) {
            System.out.println("Error, Malformed url: " + e.getMessage());
        }
    }

    private void joinServer() throws RemoteException {
        boolean serverIsFull = !remote.playerJoinServer(this.username);

        if (serverIsFull) {
            System.out.println("Server is currently full, you will be connected once there is an empty spot");

            while (!remote.playerJoinServer(username)) {
                try { Thread.sleep(100); }
                catch (InterruptedException e) { return; /* exits server if this happens */ }
            }
        }

        System.out.println("Welcome to server "+this.hostname);
    }


    private void playersServer() throws RemoteException {
        System.out.println(
                this.remote.getServerPlayers(this.username)
        );
    }

    private void disconnectServer() throws RemoteException {
        this.remote.playerQuitServer(this.username);
        this.remote = null;
        this.hostname = null;
        this.port = 0; // null
        this.username = null;
        //return; // Java complains if there's no return, although intelliJ does not like a return here ¯\_(ツ)_/¯
    }


    // mud game creation/join/status/quit
    private void createMUD(String action) throws RemoteException {
        String game_name = action.replace("create ", "");

        if (this.remote.existsMUDGameInstance(game_name)) {
            System.out.println("Error, MUD game " + game_name + " already exists");
        }

        else if (this.remote.existsMUDGameSlots()) {
            System.out.println("Error, no more MUD game slots available");
        }

        else {
            String amount = this.enterAction("Maximum number of players allowed in " + game_name +": ");
            Integer num = 1;
            try {
                num = Integer.parseInt(amount);
            }
            catch (NumberFormatException e) {
                System.out.println("Error, that was not a number");
                this.menu();
            }
            if (num < 1) {
                System.out.println("Error, cannot have " + num + " as a limit" );
                this.menu();
            }

            System.out.println(this.remote.createMUDGameInstance(game_name, this.username, num ));
        }
    }

    private void joinMUDGame(String mud_name) throws RemoteException {
        boolean gameExists = this.remote.existsMUDGameInstance(mud_name);

        if (gameExists) {
            this.setMUDName(mud_name);
            this.play();
        }

        else {
            System.out.println("MUD game " + mud_name + " does not exist");
        }
    }

    private void playersMUD() throws RemoteException {
        System.out.println(
                this.remote.getMUDPlayers(this.username, this.mud_name)
        );
    }

    private void quitMUDGame() throws RemoteException {
        this.remote.playerQuitMUD(this.location, this.username, this.inventory, this.mud_name);
        this.playing = false;

        System.out.println("You have quitMUDGame " + this.mud_name);
        this.mud_name = "";

        this.menu();
    }

    public void abort() throws RemoteException {
        // check if we're connected, 0 is an invalid port number
        if (this.port != 0) {
            if (!this.mud_name.equals("")) {
                this.quitMUDGame();
            }
            this.disconnectServer();
            System.err.println("User aborted. Shutting application");
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
        String users = this.remote.playerLook(this.location, this.mud_name);
        users = users.replaceAll("\\b"+this.username+"\\b", "<You>");

        System.out.println(
                "Node: " + this.location + users
        );
    }

    private void take(String item) throws RemoteException {
        boolean item_exists = this.remote.playerTake(this.location, item, this.inventory, this.mud_name);

        if(item_exists) {
            for(int i=0; i<this.inventory.size(); i++){
                if (this.inventory.get(i).equals("[ ]")) {
                    this.inventory.set(i, "[" + item + "]");
                    System.out.println("You have added " + item + " to your inventory");
                    this.checkInventory();
                    break;
                }
            }
        }

        else if (!this.inventory.contains("[ ]")) {
            System.out.println("You feel the crushing weight of your backpack and decide not to take " + item);
        }

        else {
            System.out.println("I cannot find the item " + item);
        }
    }

    private void checkInventory() {
        String inv = "";
        for( String inventorySspace : this.inventory) {
            inv = inv.concat(inventorySspace + " ");
        }

        System.out.println(
                "Your inventory: " + inv
        );
    }

    private void help() {
        System.out.println(
                "\t~-~-~-~-~-~-~-~-~-~-~> H E L P <~-~-~-~-~-~-~-~-~-~-~\n" +

                "\t|\t  GAME:\tQuit the mud game \t\t[quit]\t\t\t|\n" +
                "\t|\t\t\tWho is in the server \t[players server]|\n" +
                "\t|\t\t\tWho is in the mud game \t[players game]\t|\n" +
                "\t|\t\t\tBoth players checks \t[players]\t\t|\n" +
                "\t|\t\t\tHow to play \t\t\t[help]\t\t\t|\n" +

                "\t|===================================================|\n" +

                "\t|\tPLAYER:\tLook around you \t\t[look]\t\t\t|\n" +
                "\t|\t\t\tTake an item \t\t\t[take <item>]\t|\n" +
                "\t|\t\t\tCheck your inventory \t[inventory]\t\t|\n" +

                "\t|===================================================|\n" +

                "\t| MOVEMENT:\tMove north \t\t\t\t[north]\t\t\t|\n" +
                "\t|\t\t\tMove west \t\t\t\t[west]\t\t\t|\n" +
                "\t|\t\t\tMove south \t\t\t\t[south]\t\t\t|\n" +
                "\t|\t\t\tMove east \t\t\t\t[east]\t\t\t|\n" +

                "\t~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~\n"
        );
    }


    // game loop
    private void play() throws RemoteException {
        if(!this.remote.playerJoinMUD(this.username, this.mud_name)) {
            System.out.println("Error, cannot join the MUD game at the moment as it is full. Try again later");
            menu();
        }
        else {
            System.out.println("Welcome to MUD game " + this.mud_name);

            this.help();
            // game state vars
            this.playing = true;
            String action;

            // set starting loc
            this.setLocation(remote.setPlayerStartLocation(this.username, this.mud_name));

            try {
                while(this.playing) {
                    // sanitize action text
                    action = enterAction().toLowerCase().trim();

                    if (action.equals("help")) { this.help(); }
                    else if (action.equals("quit")) { this.quitMUDGame(); }
                    else if (action.equals("look")) { this.look(); }
                    else if (action.equals("inventory")) { this.checkInventory(); }
                    else if (action.equals("players game")) { this.playersMUD(); }
                    else if (action.equals("players server")) { this.playersServer(); }
                    else if (action.equals("players")) { this.playersServer(); this.playersMUD(); }
                    else if (action.startsWith("take ")) { this.take(action.replace("take ", "")); }
                    else if (action.matches("north|west|south|east")) { this.move(action); }
                    else { System.out.println("Error, command " + action + " does not exist"); this.help(); }
                }
            }
            catch (ConnectException e) { this.abort(); }
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
            return enterAction(msg);
        }
    }


    // creator
    ClientImplementation(String _hostname, int _port) throws RemoteException {
        System.setProperty("java.security.policy", ".policy");
        System.setSecurityManager(new SecurityManager());
        try {
            this.setHostname(_hostname);
            this.setPort(_port);

            this.connectServer(); // connects to the rmi server

            this.setName(this.enterAction("Enter your username: "));

            while (this.remote.playerExists(this.username)) {
                System.out.println("Error, a user with the name " + this.username + " already exists in the server");
                this.setName(this.enterAction("Enter a different username: "));
            }


            this.joinServer(); // actually joinServer the connected server after basic verification

            this.menu(); // loads up the game menu
        }
        catch (NullPointerException e) { this.abort(); }
    }
}
