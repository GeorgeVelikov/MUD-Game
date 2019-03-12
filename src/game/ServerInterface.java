package game;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    String playersOnline() throws RemoteException;
    String playerJoin(String username) throws RemoteException;
    String playerQuit(String username) throws RemoteException;
}
