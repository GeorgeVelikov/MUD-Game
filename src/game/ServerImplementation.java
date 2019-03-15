package game;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.Naming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.InetAddress;


public class ServerImplementation implements ServerInterface {
    private String serverName;
    private List<String> players = new ArrayList<>(); // list of players
    private Integer serverMaxPlayers; // hard limit on players
    private boolean serverUsed = false; // lock

    private Map<String, MUD> allMUDGames = new HashMap<>(); // all muds on the server
    private Integer maxMUDGames; // hard limit on muds on server

    private Integer playerInventoryLimit;

    // player menu's are calculated in server side
    public String menu() {
        String msg = "\nMENU" + "\t\t\t\t\t\t\t\t\t\t" +
                     "\t|Games on server " + this.allMUDGames.keySet().size() + "/" + this.maxMUDGames +
                     "\t|Users on server " + this.players.size() + "/" + this.serverMaxPlayers;
        msg += "\n\tExit server \t\t\t[Exit]";
        msg += "\n\tCreate a new mud game \t[Create <gamename>]";
        msg += "\n\tJoin a mud game \t\t[Join <gamename>]";

        if(allMUDGames.size() <= 0) {
            msg += "\n\t\t-> No mud games exist";
        }
        else {
            for (int i = 0; i < allMUDGames.keySet().size(); i++) {
                String key = allMUDGames.keySet().toArray()[i].toString();
                Integer curPlayerCount = allMUDGames.get(key).getMUDPlayers().size();
                Integer maxPlayerCount = allMUDGames.get(key).getMUDPlayerLimit();
                msg = msg.concat("\n\t\t-> " + key + "\t\t(" + curPlayerCount + "/" + maxPlayerCount + ")");
            }
        }


        return msg;
    }


    // server verifications & notifications
    public boolean playerExists(String name) {

        return this.players.contains(name);
    }

    public boolean existsMUDGameInstance(String mud_name) {

        return this.allMUDGames.keySet().contains(mud_name);
    }

    public void notification(String msg) {

        System.out.println(msg);
    }


    // getters
    public String getServerPlayers() {
        String msg = "These players are online: ";

        for(String p : this.players) {
            msg = msg.concat(p + ", ");
        }
        return msg;
    }

    public String getMUDPlayers(String username, String mud_name) {
        while (this.serverUsed) {
            assert true; // waits until it's free
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);
        String msg = "\nPlayers currently in this MUD: ";

        for(String user : currentMUD.getMUDPlayers()) {
            if (user.equals(username))
                user = "<You>";
            msg = msg.concat(user + ", ");
        }

        this.setServerIsNotUsed();

        return msg;
    }

    public Integer getPlayerLimitInventory() {

        return this.playerInventoryLimit;
    }

    private MUD getCurrentMUD(String mud_name) {

        return this.allMUDGames.get(mud_name);
    }


    // players ping the server to do something
    public boolean playerJoinServer(String username) {
        if (this.players.size() < this.serverMaxPlayers) {
            this.players.add(username);
            this.notification(username + " has joined the server");
            return true;
        }

        this.notification(username + " has attempted to join the server. Server is full");
        return false;

    }

    public void playerQuitServer(String username) {
        this.notification(
                "User " + username + " has left the server. Server capacity " +
                this.players.size() + "/" + this.serverMaxPlayers
        );
        this.players.remove(username);
    }

    public boolean playerJoinMUD(String username, String mud_name) {
        while (this.serverUsed) {
            assert true; // waits until it's free
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);

        if (currentMUD.addMUDPlayer(username)) {
            this.notification("User " + username + " has joined MUD game " + mud_name + " " +
                    currentMUD.getMUDPlayers().size() + "/" + currentMUD.getMUDPlayerLimit());
            this.setServerIsNotUsed();
            return true;

        }
        else {
            this.notification("User " + username + " attempted to join MUD game " + mud_name + " " +
                    currentMUD.getMUDPlayers().size() + "/" + currentMUD.getMUDPlayerLimit());
            this.setServerIsNotUsed();
            return false;
        }
    }

    public void playerQuitMUD(String location, String username, List<String> inventory, String mud_name) {
         while (this.serverUsed) {
            assert true; // waits until it's free
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);
        currentMUD.removePlayer(location, username);
        currentMUD.removeMUDPlayer(username);

        for(String item : inventory) {
            if (!item.equals("[ ]")) {
                item = item.replace("[", "").replace("]", "");
                currentMUD.addThing(location, item);
            }
        }

        this.setServerIsNotUsed();

        this.notification("\n" + username + " has quit MUD game " + mud_name);
    }

    public String playerLook(String location, String mud_name) {
        while (this.serverUsed) {
            assert true; // waits until it's free
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);
        String msg = currentMUD.locationInfo(location);

        this.setServerIsNotUsed();

        return msg;
    }

    public String playerMove(String user_loc, String user_move, String user_name, String mud_name) {
        while (this.serverUsed) {
            assert true; // waits until it's free
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);
        String msg = currentMUD.movePlayer(user_loc, user_move, user_name);

        this.setServerIsNotUsed();

        return msg;
    }

    public boolean playerTake(String loc, String item, List<String> inventory, String mud_name) {
        while (this.serverUsed) {
            assert true; // waits until it's free
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);

        boolean userHasSpace = inventory.contains("[ ]");
        boolean itemExists = currentMUD.takeItem(loc, item);

        if (itemExists && userHasSpace) {
            currentMUD.delThing(loc, item);
            this.setServerIsNotUsed();
            return true;
        }

        if (userHasSpace) {
            this.setServerIsNotUsed();
            return true;
        }

        else {
            this.setServerIsNotUsed();
            return false;
        }
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

    public String createMUDGameInstance(String mud_name, Integer player_max) {
        // MUD game limit on server
        Integer game_limit = 4;

        if (this.allMUDGames.size() < game_limit) {
            String edges = "./args/mymud.edg";
            String messages = "./args/mymud.msg";
            String things = "./args/mymud.thg";
            System.out.println("\nMUD game of the name " + mud_name + " has been created");
            MUD mud_map = new MUD(edges, messages, things, player_max);
            this.allMUDGames.put(mud_name, mud_map);

            return "Game " + mud_name + " created successfully";
        }
        else {
            return "Game limit on server reached";
        }
    }


    // setters
    public String setPlayerStartLocation(String username, String mud_name) {
        while (this.serverUsed) {
            assert true; // waits until it's free
        }
        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);
        currentMUD.addPlayer(currentMUD.startLocation(), username);
        String msg = currentMUD.startLocation();

        this.setServerIsNotUsed();

        this.notification("User " + username + " has joined MUD game " + mud_name);

        return msg;
    }

    public List<String> setPlayerInventory() {
        List<String> inventory = new ArrayList<>();
        for(int i=0; i<this.getPlayerLimitInventory(); i++) {
            inventory.add("[ ]");
        }
        return inventory;
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

    private void setServerPlayerInventoryLimit(Integer amount) {

        this.playerInventoryLimit = amount;
    }


    // creator for server
    ServerImplementation(int port_registry,
                         int port_server,
                         int limitMUD,
                         int limitPlayers,
                         int limitInventory) throws RemoteException {
        // runs rmiregistry automatically for the registry port specified before it creates the server
        LocateRegistry.createRegistry(port_registry);

        createServer(port_registry, port_server);

        // CONTROLS FOR SERVER SPECS, I would have made them nicer if the project was bigger, being passed as args etc.
        this.setServerMaxMUDs(limitMUD);
        this.setServerMaxPlayers(limitPlayers);
        this.setServerPlayerInventoryLimit(limitInventory);

        /*
         * create a default mud instance as to always have a game in it
         * even if you remove it, the game will handle it and show a special message
        */
        this.createMUDGameInstance("default", limitPlayers);
    }
}
