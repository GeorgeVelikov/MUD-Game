package game;

import java.rmi.RemoteException;
import java.rmi.Remote;

import java.util.List;


public interface ServerInterface extends Remote {
    void notification(String msg) throws RemoteException;

    // always inverted
    boolean playerJoinServer(String username) throws RemoteException;
    void playerQuitServer(String username) throws RemoteException;
    String getServerPlayers() throws RemoteException;

    boolean playerJoinMUD(String username, String mud_name) throws RemoteException;
    void playerQuitMUD(String location, String username, List<String> inventory, String mud_name) throws RemoteException;
    String getMUDPlayers(String username, String mud_name) throws RemoteException;


    String menu() throws RemoteException;
    String createMUDGameInstance(String mud_name, Integer player_max) throws RemoteException;
    boolean existsMUDGameInstance(String mud_name) throws RemoteException;

    String playerStartLocation(String username, String mud_name) throws RemoteException;
    String playerLook(String location, String mud_name) throws RemoteException;
    String playerMove(String user_loc, String user_move, String user_name, String mud_name) throws RemoteException;
    List<String> playerSetInventory() throws RemoteException;
    boolean playerTake(String loc, String item, List<String> inventory, String mud_name) throws RemoteException;
    boolean playerExists(String name) throws RemoteException;

    Integer getPlayerLimitInventory() throws RemoteException;

}
