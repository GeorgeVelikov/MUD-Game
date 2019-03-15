package game;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    void playerJoin(String username) throws RemoteException;
    void playerQuit(String location, String username, String mud_name) throws RemoteException;


    boolean playerTake(String loc, String item, String mud_name) throws RemoteException;
    String playersOnline() throws RemoteException;
    String playerStartLocation(String username, String mud_name) throws RemoteException;
    String playerLook(String location, String mud_name) throws RemoteException;
    String playerMove(String user_loc, String user_move, String user_name, String mud_name) throws RemoteException;

    boolean playerExists(String name) throws RemoteException;
    boolean gameExists(String mud_name) throws RemoteException;
    String menu() throws RemoteException;

    String createMUDGameInstance(String mud_name) throws RemoteException;


}
