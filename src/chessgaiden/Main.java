package chessgaiden;
import java.lang.reflect.*;
// This class sets a Game in motion. It also initialises the Moves class.
public class Main
{
    // MAIN
    public static void main(String[] args) throws InvocationTargetException, InterruptedException
    {
        Moves moves = new Moves();  // not used, just initialised.
        
        Game game = new Game();
        game.gameLoop();
    }
}