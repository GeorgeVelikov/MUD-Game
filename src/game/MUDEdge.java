package game;

// Represents an path in the game.MUD (an edge in a graph).
class MUDEdge
{
    public MUDVertex _dest;   // Your destination if you walk down this path
    public String _view;   // What you see if you look down this path
    
    public MUDEdge(MUDVertex d, String v )
    {

        _dest = d;
	    _view = v;
    }
}

