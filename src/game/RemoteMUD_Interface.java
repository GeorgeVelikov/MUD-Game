package game;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteMUD_Interface extends Remote{

    void mudInstanceNew( String name ) throws RemoteException;

    boolean mudInstanceSelect ( String name ) throws RemoteException;

    String mudInstanceList() throws RemoteException;

    String moveThing( String loc,
                      String dir,
                      String thing
    ) throws RemoteException;

    String locationInfo( String loc ) throws RemoteException;

    String startLocation() throws RemoteException;


    void addThing( String loc,
                   String thing
    ) throws RemoteException;

    void delThing( String loc,
                   String thing
    ) throws RemoteException;
}
