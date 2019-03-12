package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public String readInput(String msg) {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(msg);

        try {
            return input.readLine();
        }
        catch(IOException e) {
            return "";
        }
    }

    ClientImplementation(String _hostname, int _port, String _username) {
        this.hostname = _hostname;
        this.port = _port;
        setName(_username);
    }
}
