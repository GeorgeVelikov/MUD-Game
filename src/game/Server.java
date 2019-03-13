package game;

import java.rmi.RemoteException;

public class Server {
    // TODO: ServerManager that can have multiple servers on it
    // TODO: User limit on server
    public static void main(String[] args) throws RemoteException {
        if (args.length != 3) {
            System.err.println("Usage: MUDServer <edgesfile> <messagesfile> <thingsfile>");
            return;
        }

        String edges = args[0];
        String messages = args[1];
        String things = args[2];

        new MUD(edges, messages, things);
        new ServerImplementation(50014, 50015);
        System.out.println("Server is running boye");
    }
}
