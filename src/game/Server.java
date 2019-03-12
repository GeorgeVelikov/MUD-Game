package game;

public class Server {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: MUDServer <edgesfile> <messagesfile> <thingsfile>");
            return;
        }

        MUD mud = new MUD(args[0], args[1], args[2]);

        ServerImplementation s = new ServerImplementation(50014, 50015);
        System.out.println("Server is running boye");
    }
}
