package game;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;

public class ServerImplementation {
    public String playersOnline() {
        return "These players are online";
    }

    private static void createServer(int port_registry, int port_server){
        try {

            String hostname     = "localhost";
            System.out.println("ServerImplementation created on " + port_registry);

            System.setProperty("java.security.policy", ".policy");
            System.setSecurityManager(new SecurityManager());

            RemoteMUD mud_service = new RemoteMUD();

            RemoteMUD_Interface mud_interface = (RemoteMUD_Interface)UnicastRemoteObject.exportObject(mud_service, port_server);

            Naming.rebind("rmi://" + hostname + ":" + port_registry + "/mud", mud_interface);

            System.out.println( "Hostname: " + hostname +
                                "\nServerImplementation port: " + port_server +
                                "\nRegistry port: " + port_registry
            );


            mud_service.mudInstanceNew("Vanilla");
        }

        catch(Exception e) {
            if (e.getMessage().equals("0"))
                System.err.println("Error: Insufficient arguments.");
            else
                System.err.println("Error: " + e.getMessage());
        }
    }

    public ServerImplementation(int port_registry, int port_server) {
        createServer(port_registry, port_server);
    }

}
