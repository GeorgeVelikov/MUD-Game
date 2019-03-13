package game;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientImplementation implements ClientInterface {
    private ServerInterface remote;
    private String hostname;
    private String username;
    private int port;

    public void setName(String name) {

        this.username = name;
    }

    public void setHostname(String name) {

        this.hostname = name;
    }

    public void setPort(int _port) {

        this.port = _port;
    }

    public void join() throws RemoteException {
        System.out.println(
                remote.playerJoin(this.username)
        );
    }

    public void quit() throws RemoteException {
        System.out.println(
                remote.playerQuit(this.username)
        );
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

    ClientImplementation(String _hostname, int _port, String _username) {
        this.hostname = _hostname;
        this.port = _port;
        setName(_username);

        System.setProperty("java.security.policy", ".policy");
        System.setSecurityManager(new SecurityManager());
    }
}
