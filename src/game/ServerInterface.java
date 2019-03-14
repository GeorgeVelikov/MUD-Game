package game;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    String playerJoin(String username) throws RemoteException;
    String playerQuit(String username) throws RemoteException;
    String playersOnline() throws RemoteException;
    String playerStartLocation(String username) throws RemoteException;
    String playerLook(String location) throws RemoteException;
    String playerMove(String user_loc, String user_move, String user_name) throws RemoteException;

    boolean playerTake(String loc, String item) throws RemoteException;
}
