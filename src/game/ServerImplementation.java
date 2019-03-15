package game;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServerImplementation implements ServerInterface {
    private String serverName;
    private List<String> players = new ArrayList<>(); // list of players
    private Integer serverMaxPlayers; // hard limit on players
    private boolean serverUsed = false; // lock

    private Map<String, MUD> allMUDGames = new HashMap<>(); // all muds on the server
    private Integer maxMUDGames; // hard limit on muds on server

    /*
     * surely there has to be a better way than this,
     * this holds the current mud instance operations
     * that are being processed by the server
    */
    private MUD currentMUD;


    // player menu's are calculated in server side
    public String menu() {
        String msg = "\nMENU" + "\t\t\t\t\t\t\t\t\t\t" +
                     "\t|Games on server " + this.allMUDGames.keySet().size() + "/" + this.maxMUDGames +
                     "\t|Users on server " + this.players.size() + "/" + this.serverMaxPlayers;

        msg += "\n\tCreate a new mud game \t(Create <gamename>)";
        msg += "\n\tJoin a mud game \t\t(Join <gamename>)";

        if(allMUDGames.size() <= 0) {
            msg += "\n\t\t-> No mud games exist";
        }
        else {
            for (int i = 0; i < allMUDGames.keySet().size(); i++) {
                msg = msg.concat("\n\t\t-> " + allMUDGames.keySet().toArray()[i]);
            }
        }


        return msg;
    }


    // server verifications & notifications
    public boolean playerExists(String name) {

        return this.players.contains(name);
    }

    public boolean gameExists(String mud_name) {

        return this.allMUDGames.keySet().contains(mud_name);
    }

    public void notification(String msg) {

        System.out.println(msg);
    }


    // players join/quit/check server status
    public boolean playerJoin(String username) {
        // TODO: server client number reached, add return for user

        if (this.players.size() < this.serverMaxPlayers) {
            this.players.add(username);
            this.notification("\n" + username + " has joined the server");
            return true;
        }
        else {
            this.notification("\n" + username + " has attempted to join the server. Server is full");
            return false;
        }
    }

    public void playerQuitServer(String username) {
        this.notification(
                "User " + username + " has left the server. Server capacity " +
                this.players.size() + "/" + this.serverMaxPlayers
        );
        this.players.remove(username);
    }

    public void playerQuitMUD(String location, String username, String mud_name) {
         while (this.serverUsed) {
            assert true; // waits until it's free
        }

        this.setServerIsUsed();
        this.setMUDGameInstance(mud_name);
        this.currentMUD.removePlayer(location, username);
        this.setServerIsNotUsed();

        this.notification("\n" + username + " has quit MUD game " + mud_name);
    }

    public String playersOnline() {

        return "These players are online: " + this.players;
    }


    // players ping server to do some calculation/transformation
    public String playerStartLocation(String username, String mud_name) {
        while (this.serverUsed) {
            assert true; // waits until it's free
        }
        this.setServerIsUsed();

        this.setMUDGameInstance(mud_name);
        this.currentMUD.addPlayer(currentMUD.startLocation(), username);
        String msg = currentMUD.startLocation();

        this.setServerIsNotUsed();

        this.notification("User " + username + " has joined MUD game " + mud_name);

        return msg;
    }

    public String playerLook(String location, String mud_name) {
        while (this.serverUsed) {
            assert true; // waits until it's free
        }

        this.setServerIsUsed();

        String msg = this.currentMUD.locationInfo(location);

        this.setServerIsNotUsed();

        return msg;
    }

    public String playerMove(String user_loc, String user_move, String user_name, String mud_name) {
        while (this.serverUsed) {
            assert true; // waits until it's free
        }

        this.setServerIsUsed();

        this.setMUDGameInstance(mud_name);
        String msg = this.currentMUD.movePlayer(user_loc, user_move, user_name);

        this.setServerIsNotUsed();

        return msg;
    }

    public boolean playerTake(String loc, String item, String mud_name) {
        while (this.serverUsed) {
            assert true; // waits until it's free
        }

        this.setServerIsUsed();
        this.setMUDGameInstance(mud_name);

        boolean state = this.currentMUD.takeItem(loc, item);

        this.setServerIsNotUsed();

        return state;
    }


    // only a "server client" can create server, all clients can create mud games
    private void createServer(int port_registry, int port_server) throws RemoteException {
        try {
            this.serverName = (InetAddress.getLocalHost()).getCanonicalHostName();
        }
        catch(UnknownHostException e) {
            System.err.println("Error, cannot get hostname: " + e.getMessage());
        }

        System.setProperty("java.security.policy", ".policy");
        System.setSecurityManager(new SecurityManager());

        ServerInterface mud_interface = (ServerInterface)UnicastRemoteObject.exportObject(this, port_server);


        String url = "rmi://" + this.serverName + ":" + port_registry + "/mud";
        System.out.println("\nServer registered on " + url);

        try {

            Naming.rebind(url, mud_interface);
        }
        catch(MalformedURLException e) {
            System.err.println("Error, Malformed url: " + e.getMessage());
        }

        System.out.println( "\tHostname: \t\t" + this.serverName +
                            "\n\tServer port: \t" + port_server +
                            "\n\tRegistry port: \t" + port_registry +
                            "\n\nServer is running. . ."
        );

    }

    public String createMUDGameInstance(String mud_name) {
        // MUD game limit on server
        Integer game_limit = 4;

        if (this.allMUDGames.size() < game_limit) {
            String edges = "./args/mymud.edg";
            String messages = "./args/mymud.msg";
            String things = "./args/mymud.thg";
            System.out.println("\nMUD game of the name " + mud_name + " has been created");
            MUD mud_map = new MUD(edges, messages, things);
            this.allMUDGames.put(mud_name, mud_map);

            return "Game " + mud_name + " created successfully";
        }
        else {
            return "Game limit on server reached";
        }
    }


    // server operation setters
    private void setMUDGameInstance(String mud_name) {

        this.currentMUD = this.allMUDGames.get(mud_name);
    }

    private void setServerIsUsed() {

        this.serverUsed = true;
    }

    private void setServerIsNotUsed() {

        this.serverUsed = false;
    }

    private void setServerMaxPlayers(Integer amount) {

        this.serverMaxPlayers = amount;
    }

    private void setServerMaxMUDs(Integer amount) {

        this.maxMUDGames = amount;
    }


    // creator for server
    ServerImplementation(int port_registry, int port_server) throws RemoteException {
        createServer(port_registry, port_server);

        // CONTROLS FOR SERVER SPECS, I would have made them nicer if the project was bigger, being passed as args etc.
        this.setServerMaxPlayers(4);
        this.setServerMaxMUDs(4);

        /*
         * create a default mud instance as to always have a game in it
         * even if you remove it, the game will handle it and show a special message
        */
        this.createMUDGameInstance("default");
    }

}
