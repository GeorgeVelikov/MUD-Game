package game;

import java.rmi.RemoteException;

public class Server {
    // TODO: multiple servers eventually?
    public static void main(String[] args) throws RemoteException {
        if (args.length != 2) {
            System.err.println("Usage: MUDServer <rmiregistry port> <server port>");
            return;
        }

        Integer port_registry = Integer.parseInt(args[0]);
        Integer port_server = Integer.parseInt(args[1]);

        new ServerImplementation(port_registry, port_server, 4, 4, 4);
    }
}
