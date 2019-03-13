package game;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;


public class ServerImplementation implements ServerInterface {
    private List<String> players = new ArrayList<>();
    private String server_name;
    private MUD mud_game_map;

    public String playersOnline() {
        return "These players are online: " + this.players;
    }

    public String playerJoin(String username) {
        this.players.add(username);
        return username + " has joined the server";
    }

    public String playerQuit(String username) {
        this.players.remove(username);
        return username + " has left the server";
    }

    public MUD getMUD() {

        return this.mud_game_map;
    }

    public String playerStartLocation() {

        return mud_game_map.startLocation();
    }

    public String playerLook(String location) {

        return this.mud_game_map.locationInfo(location);
    }

    public void playerMove (Client player) {
        System.out.println(this.mud_game_map.toString());
    }

    private void createServer(int port_registry, int port_server) throws RemoteException {
        try {
            this.server_name = (InetAddress.getLocalHost()).getCanonicalHostName();
        }
        catch(UnknownHostException e) {
            System.err.println("Error, cannot get hostname: " + e.getMessage());
        }

        System.out.println("Server created on " + port_registry);

        System.setProperty("java.security.policy", ".policy");
        System.setSecurityManager(new SecurityManager());

        ServerInterface mud_interface = (ServerInterface)UnicastRemoteObject.exportObject(this, port_server);


        String url = "rmi://" + this.server_name + ":" + port_registry + "/mud";
        System.out.println("Registered on " + url);

        try {

            Naming.rebind(url, mud_interface);
        }
        catch(MalformedURLException e) {
            System.err.println("Error, Malformed url: " + e.getMessage());
        }

        System.out.println( "Hostname: " + this.server_name +
                            "\nServerImplementation port: " + port_server +
                            "\nRegistry port: " + port_registry
        );

    }

    ServerImplementation(int port_registry, int port_server, MUD map) throws RemoteException {
        this.mud_game_map = map;
        createServer(port_registry, port_server);
    }

}
