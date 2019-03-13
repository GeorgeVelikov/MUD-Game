package game;

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

    ClientImplementation(String _hostname, int _port, String _username) {
        this.hostname = _hostname;
        this.port = _port;
        setName(_username);
    }
}
