package game;

import java.util.HashMap;
import java.util.Map;


public class RemoteMUD implements RemoteMUD_Interface {

    private MUD instance;
    private Integer mud_limit = 4;
    private Integer mud_count = 0;
    Map<String, MUD> muds = new HashMap<>();

    public RemoteMUD() {

    }

    public void mudInstanceNew(String name) {
        try {
            if (mud_count.equals(mud_limit)) {
                System.out.println("ha-ha limit of mud reached boy");
            }

            else {
                muds.put(name, new MUD("./args/mymud.edg",
                                        "./args/mymud.msg",
                                        "./args/mymud.thg")
                );
            }
        }

        catch (Exception e) {
            System.err.println("Error creating mud: " + e.getMessage());
        }
    }

    public boolean mudInstanceSelect(String name) {
        if (muds.containsKey(name)) {
            instance = muds.get(name);
            return false;
        }

        else
            return true;
    }

    public String mudInstanceList() {
        String output = "Available MUD servers:\n";

        for (String key : muds.keySet()) {
            output = output.concat(key+"\n");
        }

        return output;
    }

    public String moveThing(String loc, String dir, String thing) {

        return instance.moveThing(loc, dir, thing);
    }

    public String locationInfo(String loc) {

        return instance.locationInfo(loc);
    }

    public String startLocation() {

        return instance.startLocation();
    }

    public void addThing(String loc, String thing) {

        instance.addThing(loc, thing);
    }

    public void delThing(String loc, String thing) {

        instance.delThing(loc, thing);
    }

}
