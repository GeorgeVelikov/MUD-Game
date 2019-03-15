package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;

import java.util.*;


public class MUD {
    /**
     * Private stuff
     */

    // A record of all the vertices in the game.MUD graph. HashMaps are not
    // synchronized, but we don't really need this to be synchronised.
    private Map<String, MUDVertex> vertexMap = new HashMap<>();
    private List<String> playersMUD = new ArrayList<>();
    private Integer playersMUDLimit;
    private String _startLocation = "";

    /**
     * Add a new edge to the graph.
     */
    private void addEdge(String sourceName,
                         String destName,
                         String direction,
                         String view) {
        MUDVertex v = getOrCreateVertex(sourceName);
        MUDVertex w = getOrCreateVertex(destName);
        v._routes.put(direction, new MUDEdge(w, view));
    }

    public boolean addMUDPlayer(String username) {
        if (this.playersMUD.size() < this.playersMUDLimit) {
            this.playersMUD.add(username);
            return true;
        } else {
            return false;
        }
    }

    public void removeMUDPlayer(String username) {

        this.playersMUD.remove(username);
    }

    public Integer getMUDPlayerLimit() {

        return this.playersMUDLimit;
    }

    public List<String> getMUDPlayers() {

        return this.playersMUD;
    }

    /**
     * Create a new thing at a location.
     */
    private void createThing(String loc, String thing) {
        MUDVertex v = getOrCreateVertex(loc);
        v._things.add(thing);
    }

    /**
     * Change the message associated with a location.
     */
    private void changeMessage(String loc, String msg) {
        MUDVertex v = getOrCreateVertex(loc);
        v._msg = msg;
    }

    /**
     * If vertexName is not present, add it to vertexMap.  In either
     * case, return the game.MUDVertex. Used only for creating the game.MUD.
     */
    private MUDVertex getOrCreateVertex(String vertexName) {
        MUDVertex v = vertexMap.get(vertexName);
        if (v == null) {
            v = new MUDVertex(vertexName);
            vertexMap.put(vertexName, v);
        }
        return v;
    }

    /**
     *
     */
    private MUDVertex getVertex(String vertexName) {
        return vertexMap.get(vertexName);
    }

    /**
     * Creates the edges of the graph on the basis of a file with the
     * following fromat:
     * source direction destination message
     */
    private void createEdges(String edgesfile) {
        try {
            FileReader fin = new FileReader(edgesfile);
            BufferedReader edges = new BufferedReader(fin);
            String line;

            while ((line = edges.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);

                if (st.countTokens() < 3) {
                    System.err.println("Skipping ill-formatted line " + line);
                    continue;
                }

                String source = st.nextToken();
                String dir = st.nextToken();
                String dest = st.nextToken();
                String msg = "";
                while (st.hasMoreTokens()) {
                    msg = msg.concat(st.nextToken() + " ");
                }

                addEdge(source, dest, dir, msg);
            }
        } catch (IOException e) {
            System.err.println("Graph.createEdges( String " + edgesfile + ")\n" + e.getMessage());
        }
    }

    /**
     * Records the messages assocated with vertices in the graph on
     * the basis of a file with the following format:
     * location message
     * The first location is assumed to be the starting point for
     * users joining the game.MUD.
     */
    private void recordMessages(String messagesfile) {
        try {
            FileReader fin = new FileReader(messagesfile);
            BufferedReader messages = new BufferedReader(fin);
            String line;
            boolean first = true; // For recording the start location.
            while ((line = messages.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);

                if (st.countTokens() < 2) {
                    System.err.println("Skipping ill-formatted line " + line);
                    continue;
                }

                String loc = st.nextToken();
                String msg = "";

                while (st.hasMoreTokens()) {
                    msg = msg.concat(st.nextToken() + " ");
                }

                changeMessage(loc, msg);
                if (first) {      // Record the start location.
                    _startLocation = loc;
                    first = false;
                }
            }
        } catch (IOException e) {
            System.err.println("Graph.recordMessages( String " + messagesfile + ")\n" + e.getMessage());
        }
    }

    /**
     * Records the things assocated with vertices in the graph on
     * the basis of a file with the following format:
     * location thing1 thing2 ...
     */
    private void recordThings(String thingsfile) {
        try {
            FileReader fin = new FileReader(thingsfile);
            BufferedReader things = new BufferedReader(fin);
            String line;

            while ((line = things.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);

                if (st.countTokens() < 2) {
                    System.err.println("Skipping ill-formatted line " + line);
                    continue;
                }

                String loc = st.nextToken();

                while (st.hasMoreTokens()) {
                    addThing(loc, st.nextToken());
                }
            }
        } catch (IOException e) {
            System.err.println("Graph.recordThings( String " +
                    thingsfile + ")\n" + e.getMessage());
        }
    }

    /*
     * All the public stuff. These methods are designed to hide the
     * internal structure of the game.MUD. Could declare these on an
     * interface and have external objects interact with the game.MUD via
     * the interface.
     */

    /**
     * A constructor that creates the game.MUD.
     */
    public MUD(String edgesfile, String messagesfile, String thingsfile, Integer player_limit) {
        createEdges(edgesfile);
        recordMessages(messagesfile);
        recordThings(thingsfile);

        this.playersMUDLimit = player_limit;

        System.out.println("Files read...");
        System.out.println(vertexMap.size() + " vertices\n");
    }

    // This method enables us to display the entire game.MUD (mostly used
    // for testing purposes so that we can check that the structure
    // defined has been successfully parsed.
    public String toString() {
        String summary = "";
        Iterator iter = vertexMap.keySet().iterator();
        String loc;

        while (iter.hasNext()) {
            loc = (String) iter.next();
            summary = summary.concat("Node: " + loc);
            summary += (vertexMap.get(loc)).toString();
        }

        summary += "Start location = " + _startLocation;
        return summary;
    }

    /**
     * A method to provide a string describing a particular location.
     */
    public String locationInfo(String loc) {
        return getVertex(loc).toString();
    }

    /**
     * Get the start location for new game.MUD users.
     */
    public String startLocation() {
        return _startLocation;
    }

    /**
     * Add a thing to a location; used to enable us to add new users.
     */
    public void addThing(String loc, String thing) {
        MUDVertex v = getVertex(loc);
        v._things.add(thing);
    }

    /**
     * Remove a thing from a location.
     */
    public void delThing(String loc, String thing) {
        MUDVertex v = getVertex(loc);

        if (v._things.contains(thing)) {
            String msg = "Evidence of a ";

            if (thing.endsWith("s")) {
                msg = msg.replace("a ", "");
            }

            if (!v._things.contains(msg)) {
                this.createThing(loc, msg + thing + " residing here before");
            }
            v._things.remove(thing);
        }
    }

    public void removePlayer(String loc, String user) {
        MUDVertex v = getVertex(loc);
        v._players.remove(user);
    }

    /**
     * A method to enable a player to move through the game.MUD (a player
     * is a thing). Checks that there is a route to travel on. Returns
     * the location moved to.
     */
    public String moveThing(String loc, String dir, String thing) {
        MUDVertex v = getVertex(loc);
        MUDEdge e = v._routes.get(dir);
        if (e == null)   // if there is no route in that direction
            return loc;  // no move is made; return current location.
        v._things.remove(thing);
        e._dest._things.add(thing);
        return e._dest._name;
    }

    public void addPlayer(String loc, String player) {
        MUDVertex v = getVertex(loc);
        v._players.add(player);
    }


    public String movePlayer(String loc, String dir, String player) {
        MUDVertex v = getVertex(loc);
        MUDEdge e = v._routes.get(dir);
        if (e == null)   // if there is no route in that direction
            return loc;  // no move is made; return current location.
        v._players.remove(player);
        e._dest._players.add(player);
        return e._dest._name;
    }

    /* user can take item from loc */
    public boolean takeItem(String loc, String item) {
        MUDVertex current_vertex = getVertex(loc);
        List<String> items = current_vertex._things;

        return items.contains(item);
    }
}

