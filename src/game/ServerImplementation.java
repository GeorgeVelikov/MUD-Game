package game;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerImplementation implements ServerInterface {
    public String playersOnline() {
        return "These players are online";
    }

    private void createServer(int port_registry, int port_server) throws RemoteException {
        String hostname = "undefined";

        try {
            hostname = (InetAddress.getLocalHost()).getCanonicalHostName();
        }
        catch(UnknownHostException e) {
            System.err.println("Error, cannot get hostname: " + e.getMessage());
        }

        System.out.println("Server created on " + port_registry);

        System.setProperty("java.security.policy", ".policy");
        System.setSecurityManager(new SecurityManager());

        ServerInterface mud_interface = (ServerInterface)UnicastRemoteObject.exportObject(this, port_server);


        String url = "rmi://" + hostname + ":" + port_registry + "/mud";
        System.out.println("Registered on " + url);

        try {

            Naming.rebind(url, mud_interface);
        }
        catch(MalformedURLException e) {
            System.err.println("Error, Malformed url: " + e.getMessage());
        }

        System.out.println( "Hostname: " + hostname +
                            "\nServerImplementation port: " + port_server +
                            "\nRegistry port: " + port_registry
        );

    }

    public ServerImplementation(int port_registry, int port_server) throws RemoteException {

        createServer(port_registry, port_server);
    }

}
