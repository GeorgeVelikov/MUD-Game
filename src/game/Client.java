package game;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

public class Client {
    private static String enterName() {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        try {
            return input.readLine();
        }
        catch(IOException e) {
            return "";
        }
    }

    public static void main(String[] args) throws RemoteException {
        String hostname = "";
        int port = 0;

        try {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.err.println("Error, illegal arguments: " + e.getMessage());
        }

        System.out.print("Joining " + hostname + "\nPlease enter your username: ");
        String _username = enterName();

        ClientImplementation client = new ClientImplementation(hostname, port, _username);

        client.connect();

        client.join();
        client.checkClients();
        client.play();
        client.quit();
    }
}
