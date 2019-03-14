package game;

import java.rmi.RemoteException;

public class Server {
    // TODO: ServerManager that can have multiple servers on it
    // TODO: User limit on server
    public static void main(String[] args) throws RemoteException {
        if (args.length != 2) {
            System.err.println("Usage: MUDServer <rmiregistry port> <server port>");
            return;
        }

        Integer port_registry = Integer.parseInt(args[0]);
        Integer port_server = Integer.parseInt(args[1]);

        new ServerImplementation(port_registry, port_server);
    }
}
