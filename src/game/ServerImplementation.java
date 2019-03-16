package game;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.Naming;

import java.text.SimpleDateFormat;
import java.util.*;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.InetAddress;


public class ServerImplementation implements ServerInterface {
    private String serverName;
    private List<String> players = new ArrayList<>(); // list of players
    private Integer serverMaxPlayers; // hard limit on players
    private boolean serverUsed = false; // lock
    private List<String> serverPlayersInQue = new ArrayList<>();

    private Map<String, MUD> allMUDGames = new HashMap<>(); // all muds on the server
    private Integer maxMUDGames; // hard limit on muds on server

    private Integer playerInventoryLimit;

    // player menu's are calculated in server side
    public String menu() {
        String msg = "MENU" + "\t\t\t\t\t\t\t\t\t\t" +
                     "\t|Games on server " + this.allMUDGames.keySet().size() + "/" + this.maxMUDGames +
                     "\t|Users on server " + this.players.size() + "/" + this.serverMaxPlayers;
        msg += "\n\tExit server \t\t\t[Disconnect]";
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

    public void notification(String msg, boolean error) {

        SimpleDateFormat formatter = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss] ");
        String date = formatter.format(new Date());

        if (!error) {
            System.out.println(date + msg);
        }

        else {
            System.err.println(date + msg);
        }
    }


    // getters
    public String getServerPlayers(String username) {
        String msg = "These players are online in server " + this.serverName + ": ";

        for(String user : this.players) {
            if (user.equals(username))
                user = "<You>";
            msg = msg.concat(user + ", ");
        }
        return msg;
    }

    public String getMUDPlayers(String username, String mud_name) {
        while (this.serverUsed) {
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);
        String msg = "Players currently in this MUD: ";

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
        /* Not using a proper que right now it's kind of random as the person to ping the server after
         * a 100ms sleep first gets the spot (better than a stack, but still kind of based on luck, which can be worse)
         * If I were to properly implement this, i'd make a player class and have a List<Player> que
         * Which the server will control here
        */
        if (this.players.size() < this.serverMaxPlayers) {
            if (this.serverPlayersInQue.contains(username)) {
                this.serverPlayersInQue.remove(username);
            }
            this.players.add(username);
            this.notification("\tUser " + username + " has joined the server. Server capacity " +
                    this.players.size() + "/" + this.serverMaxPlayers,false);
            return true;
        }

        else {
            if (!this.serverPlayersInQue.contains(username)) {
                this.serverPlayersInQue.add(username);
                this.notification("\tUser " + username + " has attempted to join the server. Server is full", true);
            }
            return false;
        }

    }

    public void playerQuitServer(String username) {
        this.players.remove(username);
        this.notification("\tUser " + username + " has left the server. Server capacity " +
                this.players.size() + "/" + this.serverMaxPlayers, false);
    }

    public boolean playerJoinMUD(String username, String mud_name) {
        while (this.serverUsed) {
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                this.notification("Error, something has gone terribly wrong: " + e.getMessage(), true);
            }
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);

        if (currentMUD.addMUDPlayer(username)) {
            this.notification("\tUser " + username + " has joined MUD game " + mud_name + " " +
                    currentMUD.getMUDPlayers().size() + "/" + currentMUD.getMUDPlayerLimit(), false);
            this.setServerIsNotUsed();
            return true;

        }
        else {
            this.notification("\tUser " + username + " attempted to join MUD game " + mud_name + " " +
                    currentMUD.getMUDPlayers().size() + "/" + currentMUD.getMUDPlayerLimit(), true);
            this.setServerIsNotUsed();
            return false;
        }
    }

    public void playerQuitMUD(String location, String username, List<String> inventory, String mud_name) {
         while (this.serverUsed) {
             try {
                 Thread.sleep(100); // waits until it's free
             } catch (InterruptedException e) {
                 this.notification("Error, something has gone terribly wrong: " + e.getMessage(), true);
             }
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

        this.notification("\n" + username + " has quit MUD game " + mud_name,false);
    }

    public String playerLook(String location, String mud_name) {
        while (this.serverUsed) {
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                this.notification("Error, something has gone terribly wrong: " + e.getMessage(), true);
            }
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);
        String msg = currentMUD.locationInfo(location);

        this.setServerIsNotUsed();

        return msg;
    }

    public String playerMove(String user_loc, String user_move, String user_name, String mud_name) {
        while (this.serverUsed) {
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                this.notification("Error, something has gone terribly wrong: " + e.getMessage(), true);
            }
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);
        String msg = currentMUD.movePlayer(user_loc, user_move, user_name);

        this.setServerIsNotUsed();

        return msg;
    }

    public boolean playerTake(String loc, String item, List<String> inventory, String mud_name) {
        while (this.serverUsed) {
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                this.notification("Error, something has gone terribly wrong: " + e.getMessage(), true);
            }
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

        try {
            Naming.rebind(url, mud_interface);
        }
        catch(MalformedURLException e) {
            System.err.println("Error, Malformed url: " + e.getMessage());
        }

        this.notification( "\tServer registered on " + url +
                            "\n\t\t\t\t\t\t\tHostname: \t\t" + this.serverName +
                            "\n\t\t\t\t\t\t\tServer port: \t" + port_server +
                            "\n\t\t\t\t\t\t\tRegistry port: \t" + port_registry,false);

        this.notification("\tServer is running. . .", false);

    }

    public String createMUDGameInstance(String mud_name, Integer player_max) {
        // MUD game limit on server
        Integer game_limit = 4;

        if (this.allMUDGames.size() < game_limit) {
            // might want to eventually allow players to launch their own edges
            String edges = "./args/mymud.edg";
            String messages = "./args/mymud.msg";
            String things = "./args/mymud.thg";

            this.notification("\tMUD game of the name " + mud_name + " has been created", false);
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
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                this.notification("Error, something has gone terribly wrong: " + e.getMessage(), true);
            }
        }
        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(mud_name);
        currentMUD.addPlayer(currentMUD.startLocation(), username);
        String msg = currentMUD.startLocation();

        this.setServerIsNotUsed();

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
        // runs rmi registry automatically for the registry port specified before it creates the server
        System.out.println(""); // just to make things prettier
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
