package game;

import java.rmi.RemoteException;

public class Server {
    // The way It is structured right now is multiple servers can easily be made
    public static void main(String[] args) throws RemoteException {
        Integer port_registry = 0;
        Integer port_server = 0;

        try {
            port_registry = Integer.parseInt(args[0]);
            port_server = Integer.parseInt(args[1]);
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.err.println("Error, illegal arguments: " + e.getMessage() + "\n <rmiregistry port> <server port>");
        }

        new ServerImplementation(port_registry, port_server, 4, 4, 4);
    }
}
