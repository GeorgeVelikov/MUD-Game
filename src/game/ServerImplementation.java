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
    public boolean playerExists(ClientImplementation client) {

        return this.players.contains(client.getName());
    }

    public boolean existsMUDGameInstance(String game_name) {

        return this.allMUDGames.keySet().contains(game_name);
    }

    public boolean existsMUDGameSlots() {

        return this.allMUDGames.size() == this.maxMUDGames;
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
    public String getServerPlayers(ClientImplementation client) {
        String msg = "These players are online in server " + this.serverName + ": ";

        for(String user : this.players) {
            if (user.equals(client.getName()))
                user = "<You>";
            msg = msg.concat(user + ", ");
        }
        return msg;
    }

    public String getMUDPlayers(ClientImplementation client) {
        while (this.serverUsed) {
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(client.getMUDName());
        String msg = "Players currently in this MUD: ";

        for(String user : currentMUD.getMUDPlayers()) {
            if (user.equals(client.getName()))
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
    public boolean playerJoinServer(ClientImplementation client) {
        /* Not using a proper que right now it's kind of random as the person to ping the server after
         * a 100ms sleep first gets the spot (better than a stack, but still kind of based on luck, which can be worse)
         * If I were to properly implement this, i'd make a player class and have a List<Player> que
         * Which the server will control here
        */
        if (this.players.size() < this.serverMaxPlayers) {
            if (this.serverPlayersInQue.contains(client.getName())) {
                this.serverPlayersInQue.remove(client.getName());
            }
            this.players.add(client.getName());

            String msg = "\tUser " + client.getName() + " has joined the server. Server capacity " +
                    this.players.size() + "/" + this.serverMaxPlayers;

            Integer playersInQue = this.serverPlayersInQue.size();
            if (playersInQue > 0) {
                msg += ". There are currently " + playersInQue + " player(s) in the que waiting to join the server";
            }
            this.notification(msg,false);
            return true;
        }

        else {
            if (!this.serverPlayersInQue.contains(client.getName())) {
                this.serverPlayersInQue.add(client.getName());
                this.notification("\tUser " + client.getName() + " has attempted to join the server. " +
                        "Server is full and there are " + this.serverPlayersInQue.size() + " player(s) in the que " +
                        "waiting to join the server" , true);
            }
            return false;
        }

    }

    public void playerQuitServer(ClientImplementation client) {
        this.players.remove(client.getName());
        this.serverPlayersInQue.remove(client.getName());

        String msg = "\tUser " + client.getName() + " has left the server. Server capacity " +
                this.players.size() + "/" + this.serverMaxPlayers;

        this.notification(msg, false);
    }

    public boolean playerJoinMUD(ClientImplementation client) {
        while (this.serverUsed) {
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                this.notification("\tError, something has gone terribly wrong: " + e.getMessage(), true);
            }
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(client.getMUDName());

        if (currentMUD.addMUDPlayer(client.getName())) {
            this.notification("\tUser " + client.getName() + " has joined MUD game " + client.getMUDName() + " " +
                    currentMUD.getMUDPlayers().size() + "/" + currentMUD.getMUDPlayerLimit(), false);
            this.setServerIsNotUsed();
            return true;

        }
        else {
            this.notification("\tUser " + client.getName() + " attempted to join MUD game " + client.getMUDName() + " " +
                    currentMUD.getMUDPlayers().size() + "/" + currentMUD.getMUDPlayerLimit(), true);
            this.setServerIsNotUsed();
            return false;
        }
    }

    public void playerQuitMUD(ClientImplementation client) {
         while (this.serverUsed) {
             try {
                 Thread.sleep(100); // waits until it's free
             } catch (InterruptedException e) {
                 this.notification("\tError, something has gone terribly wrong: " + e.getMessage(), true);
             }
         }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(client.getMUDName());
        currentMUD.removePlayer(client.getLocation(), client.getName());
        currentMUD.removeMUDPlayer(client.getName());

        for(String item : client.getInventory()) {
            if (!item.equals("[ ]")) {
                item = item.replace("[", "").replace("]", "");
                currentMUD.addThing(client.getLocation(), item);
            }
        }

        this.setServerIsNotUsed();

        this.notification("\t" + client.getName() + " has quit MUD game " + client.getMUDName(),false);
    }

    public String playerLook(ClientImplementation client) {
        while (this.serverUsed) {
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                this.notification("\tError, something has gone terribly wrong: " + e.getMessage(), true);
            }
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(client.getMUDName());
        String msg = currentMUD.locationInfo(client.getLocation());

        this.setServerIsNotUsed();

        return msg;
    }

    public String playerMove(String user_move, ClientImplementation client) {
        while (this.serverUsed) {
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                this.notification("\tError, something has gone terribly wrong: " + e.getMessage(), true);
            }
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(client.getMUDName());
        String msg = currentMUD.movePlayer(client.getLocation(), user_move, client.getName());

        this.setServerIsNotUsed();

        return msg;
    }

    public boolean playerTake(String item, ClientImplementation client) {
        while (this.serverUsed) {
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                this.notification("\tError, something has gone terribly wrong: " + e.getMessage(), true);
            }
        }

        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(client.getMUDName());

        boolean userHasSpace = client.getInventory().contains("[ ]");
        boolean itemExists = currentMUD.takeItem(client.getLocation(), item);

        if (itemExists && userHasSpace) {
            currentMUD.delThing(client.getLocation(), item);
            this.setServerIsNotUsed();
            return true;
        }

        if (userHasSpace) {
            this.setServerIsNotUsed();
            return false;
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

    public String createMUDGameInstance(String mud_game, Integer player_max, ClientImplementation client) {
        if (this.allMUDGames.size() < this.maxMUDGames) {
            // might want to eventually allow players to launch their own edges
            String edges = "./args/mymud.edg";
            String messages = "./args/mymud.msg";
            String things = "./args/mymud.thg";

            this.notification("\tMUD game of the name " + mud_game + " has been created", false);
            MUD mud_map = new MUD(edges, messages, things, player_max);
            this.allMUDGames.put(mud_game, mud_map);

            return "Game " + client.getMUDName() + " created successfully";
        }
        else {
            this.notification("\tUser " + client.getName() + " has attempted to create MUD game " + mud_game +
                    ". MUD game slots on the server are full " +
                    this.allMUDGames.size() + "/" + this.maxMUDGames, true);
            return "Game limit on server reached";
        }
    }
    // creates "default" server, private because only server will ever need it
    private void createMUDGameInstance(Integer player_max) {
        if (this.allMUDGames.size() < this.maxMUDGames) {
            // might want to eventually allow players to launch their own edges
            String edges = "./args/mymud.edg";
            String messages = "./args/mymud.msg";
            String things = "./args/mymud.thg";

            this.notification("\tMUD game of the name default has been created", false);
            MUD mud_map = new MUD(edges, messages, things, player_max);
            this.allMUDGames.put("default", mud_map);
        }
        else {
            this.notification("\tNo MUD game slots available in the server", true);
        }
    }


    // setters
    public String setPlayerStartLocation(ClientImplementation client) {
        while (this.serverUsed) {
            try {
                Thread.sleep(100); // waits until it's free
            } catch (InterruptedException e) {
                this.notification("\tError, something has gone terribly wrong: " + e.getMessage(), true);
            }
        }
        this.setServerIsUsed();

        MUD currentMUD = this.getCurrentMUD(client.getMUDName());
        currentMUD.addPlayer(currentMUD.startLocation(), client.getName());
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
        this.createMUDGameInstance(limitPlayers);

        /*
         * Most of the server functions have locks. However I did c/p a lot of times
         * and definitely could have had around 50-60 less lines than what I have now.
         * I could not quite figure out a better way in time
         * I needed to make multiple lockThis(args) functions to accommodate the various locked parts
         */
    }
}
