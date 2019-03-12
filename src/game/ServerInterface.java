package game;

import java.rmi.Remote;

public interface ServerInterface extends Remote {
    String playersOnline();
}
