package game;

import java.rmi.ConnectException;
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
        ClientImplementation client;

        try {
            client = new ClientImplementation(hostname, port);
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {
                        try { client.abort(); }
                        catch (RemoteException e) {
                            System.err.println("Error, severe connection error: " + e.getMessage());
                        }
                    })
            );
        }

        catch (ConnectException e) {
            System.err.println("Server is not accessible at this time. Try again later");
        }
    }
}
