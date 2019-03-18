package game;

import java.rmi.RemoteException;
import java.rmi.Remote;

import java.util.List;


public interface ServerInterface extends Remote {
    void notification(String msg, boolean error) throws RemoteException;

    // always inverted
    boolean playerJoinServer(ClientImplementation client) throws RemoteException;
    void playerQuitServer(ClientImplementation client) throws RemoteException;
    String getServerPlayers(ClientImplementation client) throws RemoteException;

    boolean playerJoinMUD(ClientImplementation client) throws RemoteException;
    void playerQuitMUD(ClientImplementation client) throws RemoteException;
    String getMUDPlayers(ClientImplementation client) throws RemoteException;


    String menu() throws RemoteException;
    String createMUDGameInstance(String game_name, Integer player_max, ClientImplementation client) throws RemoteException;
    boolean existsMUDGameInstance(String game_name) throws RemoteException;
    boolean existsMUDGameSlots() throws RemoteException;

    String setPlayerStartLocation(ClientImplementation client) throws RemoteException;
    String playerLook(ClientImplementation client) throws RemoteException;
    String playerMove(String user_move, ClientImplementation client) throws RemoteException;
    List<String> setPlayerInventory() throws RemoteException;
    boolean playerTake(String item, ClientImplementation client) throws RemoteException;
    boolean playerExists(ClientImplementation client) throws RemoteException;

    Integer getPlayerLimitInventory() throws RemoteException;

}
