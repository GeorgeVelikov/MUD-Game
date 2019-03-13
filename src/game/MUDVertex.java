package game;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

// Represents a location in the game.MUD (a vertex in the graph).
class MUDVertex
{
    public String 				_name;             	// game.MUDVertex name
    public String 				_msg = "";         	// Message about this location
    public Map<String, MUDEdge> 	_routes; 			// Association between direction (e.g. "north") and a path (game.MUDEdge)
    public List<String> 		_things;     		// The things (e.g. players) at this location

    public MUDVertex(String nm )
    {
		_name 	= nm;
		_routes = new HashMap<>(); // Not synchronised
		_things = new Vector<>();       // Synchronised
    }

    public String toString()
    {
		String summary = "\n";
		summary += _msg + "\n";
		Iterator iter = _routes.keySet().iterator();
		String direction;

		while (iter.hasNext()) {
			direction = (String)iter.next();
			summary += "To the " + direction + " there is " + ((MUDEdge)_routes.get( direction ))._view + "\n";
		}

		iter = _things.iterator();
		if (iter.hasNext()) {
			summary += "You can see: ";

			do {
				summary += iter.next() + " ";
			} while (iter.hasNext());
		}

		summary += "\n\n";
		return summary;
    }
}
