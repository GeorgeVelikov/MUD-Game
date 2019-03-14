package game;

import java.rmi.RemoteException;

interface ClientInterface {
    void connect() throws RemoteException;
    void menu() throws RemoteException;
    void join() throws RemoteException;
    void quit() throws RemoteException;
    void disconnect() throws RemoteException;

    void players() throws RemoteException;
    void setName(String name) throws RemoteException;
    void setHostname(String name) throws RemoteException;
    void setPort(int _port) throws RemoteException;
}
