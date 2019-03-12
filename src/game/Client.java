package game;


import java.rmi.RemoteException;

public class Client {
    public static void main(String[] args) throws RemoteException {
        String hostname = "";
        String username = "";
        int port = 0;

        try {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
            username = args[2];
        }
        catch(IllegalArgumentException e) {
            System.err.println("Error, illegal arguments: " + e.getMessage());
        }

        ClientImplementation client = new ClientImplementation(hostname, port, username);

        client.join();

        client.quit();
    }
}
