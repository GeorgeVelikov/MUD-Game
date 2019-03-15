package game;

import java.rmi.RemoteException;

public class Client {
    public static void main(String[] args) throws RemoteException {
        String hostname = "";
        int port = 0;

        try {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.err.println("Error, illegal arguments: " + e.getMessage() + "\n <hostname> <registry port>");
        }

        // if more servers exist, menu exists here where a list of servers and quit is given
        new ClientImplementation(hostname, port);
    }
}
