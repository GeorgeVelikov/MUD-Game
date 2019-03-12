package game;

import java.rmi.RemoteException;

public class ClientImplementation implements ClientInterface {
    private String hostname;
    private String username;
    private int port;
    ServerInterface remote;

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

    public void setName(String name) {

        this.username = name;
    }

    public ClientImplementation(String _hostname, int _port, String _username) {
        this.hostname = _hostname;
        this.port = _port;
        setName(_username);
    }
}
