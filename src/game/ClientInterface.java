package game;

import java.rmi.RemoteException;

interface ClientInterface {
    void join() throws RemoteException;
    void quit() throws RemoteException;
    void setName(String name) throws RemoteException;
}
