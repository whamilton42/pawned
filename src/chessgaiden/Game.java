package chessgaiden;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import java.lang.reflect.*;
// This class controls the running of a single game of chess.
public class Game
{
    // <editor-fold defaultstate="collapsed" desc="Attributes">
    private boolean gameRunning = true;
    private View view;
    private Board board;
    // Counters
    public int nodeCount = 0;
    private int alphaBetaCount = 0;
    private long startTime = 0, endTime = 0;
    // History
    private Board[] history = new Board[100];
    private String historyText = "";
    // Search
    private ArrayList<String> bestMoves = new ArrayList<String>();
    private String bestMove;
    // Initial
    private int turn = 0;
    private char player = 'b';  // flips immediately
    // Utilities
    private int infinity = 200;
    // Movement
    private String movePiece = null;
    private int moveSquare = 0;
    private int destination = 0;
    // </editor-fold>

    // Options
    private int searchDepth = 4;
    private int humanPlayers = 1;
    private boolean debug = false;
    private char human = Board.WHITE;

    // CONSTRUCTOR
    public Game() throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait(view = new View(this));    // view
        board = new Board();                                    // board
    }
    ///////////////////////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////////////////////
    // Game!
    public void gameLoop()
    {
        while (gameRunning == true)
        {
            // <editor-fold defaultstate="collapsed" desc="Checkmate?">
            if (board.isCheckmate())
            {
                // Display
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        view.drawAll(board, player);
                        view.drawCheckmate();
                    }
                });

                while (1 == 1)
                {
                    // merely hangs, at the moment, for user to close window
                }
            }
            // </editor-fold>

            increment();
            history();
            displayAll();

            ///////////////////////////////////////////////////////////////////


            ///////////////////////////////////////////////////////////////////
            // Human //////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////////
            if (humanPlayers == 2 || (humanPlayers == 1 && player == human))
            {
                chooseMove_human();
            }
            ///////////////////////////////////////////////////////////////////
            // Artificial Intelligence ////////////////////////////////////////
            ///////////////////////////////////////////////////////////////////
            else
            {
                startTime = System.currentTimeMillis();     // start
                chooseMove_AI(board, player);               // do
                endTime = System.currentTimeMillis();       // end


                // Update History
                System.out.println((endTime - startTime) / 1000 + " secs, " + alphaBetaCount + " alpha-betas.");
                historyText += (endTime - startTime) / 1000 + " secs, " + alphaBetaCount + " alpha-betas.\n";
            }
            ///////////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////////

            
            // Update History
            System.out.println("move " + movePiece.charAt(0) + movePiece.charAt(1) + movePiece.charAt(2) + " from " + board.getPosition(movePiece) + " to " + destination);
            historyText += "move " + movePiece.charAt(0) + movePiece.charAt(1) + movePiece.charAt(2) 
                    + " from " + board.getPosition(movePiece)
                    + " to " + destination + "\n\n";


            // Do Move!
            board.doMove(movePiece, destination);
        }
    }
    ///////////////////////////////////////////////////////////////////////////


    public void displayAll()
    {
        // Display
        SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    view.drawAll(board, player);
                }
            }
        );
    }

    public void increment()
    {
        turn++;                             // counter
        alphaBetaCount = 0;                 // counter
        player = flipPlayer(player);        // flip player
    }

    public void history()
    {
        history[turn] = new Board(board);   // history
        
        // Update History
        System.out.println("\n=========== " + player + " ===== (" + turn + ") ========");
        historyText += "(" + turn + ") ------------ " + player + " ------------\n";
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Humanity ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    public void chooseMove_human()
    {
        // Initialise
        moveSquare = 0;
        destination = 0;
        view.setClickedSquare(0);

        // Display
        SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    view.drawBoard(true, board);
                }
            }
        );
        
        // 1. Choose Piece
        System.out.println("choose movePiece");
        do
        {
            // Get clicks
            moveSquare = view.getClickedSquare();
            movePiece = board.whatHere(moveSquare);

            // Check
            checkButtons();
        }
        // must be:
        // a) a valid square
        // b) contain a piece
        // c) contain the *player's* piece
        // d) contain a piece able to move
        while (moveSquare == 0 || movePiece.isEmpty() || movePiece.charAt(0) != player || !board.canMove(movePiece));


        view.setClickedSquare(0);       // reset click

        // Update
        SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    view.drawBoard(true, board);
                    view.highlight(moveSquare);
                }
            }
        );


        // 2. Choose Destination
        System.out.println("choose destination");
        do
        {
            // Get clicks
            destination = view.getClickedSquare();

            // Deselect piece.
            if (destination == moveSquare)
            {
                chooseMove_human();
                break;
            }
        }
        // must be:
        // a) a valid square
        // b) a move the piece can make
        while (destination == 0 || !board.moveOkay(movePiece, destination));


        view.setClickedSquare(0);   // reset click
    }
    ///////////////////////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////////////////////
    // Artificial Intelligence ////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    // Choose
    public void chooseMove_AI(Board board, char player)
    {
        // 1. Search
        double alphaBeta = -alphaBeta(board, player, searchDepth, -infinity, +infinity);
        System.out.println("ALPHA-BETA = " + alphaBeta);
        System.out.println("BEST-MOVE : " + bestMove);

        // 2. Set the pieces to be moved.
        movePiece = getPieceIDfromMove(bestMove);
        destination = getDestinationFromMove(bestMove);
    }

    // Search
    public double alphaBeta(Board board, char player, int depth, double alpha, double beta)
    {
        // <editor-fold defaultstate="collapsed" desc="debug">
        if (debug)
        {
            System.out.println(indent(depth) + "[" + depth + "] alpha = " + alpha + ", beta = " + beta);
            board.display(indent(depth));
            System.out.println();
        }
        // </editor-fold>

        alphaBetaCount++;
        
        // Evaluate
        if (depth == 0 || board.isCheckmate())
        {
            // <editor-fold defaultstate="collapsed" desc="debug">
            if (debug)
            {
                System.out.println("= " + playerToInt(player) * board.evaluate());
                System.out.println();
            }
            // </editor-fold>
            
            return playerToInt(player) * board.evaluate();
        }

        // Deeper
        // <editor-fold defaultstate="collapsed" desc="move setup">
        String[] potentialMoves = board.potentialMoves(player);
        int move = 0;
        // </editor-fold>

        while (potentialMoves[move] != null)
        {
            // <editor-fold defaultstate="collapsed" desc="Move Node">
            Board moveBoard = new Board(board);
            moveBoard.doMove(getPieceIDfromMove(potentialMoves[move]), getDestinationFromMove(potentialMoves[move]));

            // </editor-fold>

            double nextAB = -alphaBeta(moveBoard, flipPlayer(player), depth-1, -beta, -alpha);

            // Set Move
            if (depth == searchDepth)
            {
                // Is one of these better?
                if (nextAB > alpha)
                {
                    // <editor-fold defaultstate="collapsed" desc="debug">
                    if (debug)
                        System.out.println(nextAB);
                    // </editor-fold>
                    bestMove = potentialMoves[move];
                }
            }

            alpha = max(alpha, nextAB);

            // Cut-off
            if (beta <= alpha)
            {
                break;
            }

            move++;
        }

        return alpha;
    }
    //////////////////////////////////////////////////////////////////////////

    



    ///////////////////////////////////////////////////////////////////////////
    // <editor-fold defaultstate="collapsed" desc="Utilities">
    // Utilities ///////
    public static char flipPlayer(char player) {
        if (player == 'w')
            return 'b';
        else
            return 'w';
    }
    public static String flipMinMax(String minOrMax) {
        if (minOrMax.equals("max")) {
            return "min";

        } else {
            return "max";

        }
    }
    public static double max(double number1, double number2) {
        if (number1 >= number2) {
            return number1;

        } else {
            return number2;

        }
    }
    public static double min(double number1, double number2) {
        if (number1 <= number2) {
            return number1;

        } else {
            return number2;

        }
    }
    public static String indent(int number) {
        String indent = "";
        for (int count = 0; count < number; count++)
        {
            indent += "        ";
        }
        return indent;
    }
    public static int playerToInt(char player) {
        if (player == 'w')
            return 1;
        else
            return -1;
    }
    /////////////////////
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Buttons">
    public void checkButtons()
    {
        if (view.getClicked("left") != null && view.getClicked("left").equals("arrange"))
            arrangePieces();
        if (view.getClicked("left") != null && view.getClicked("left").equals("debug"))
            debugLastMove();
    }

    public void arrangePieces()
    {
        System.out.println("start arrange");
        view.setClicked(null);

        boolean arranging = true;
        while (arranging)
        {
            // 1. Choose Piece
            System.out.println("choose movePiece");
            do
            {
                moveSquare = view.getClickedSquare();
                movePiece = board.whatHere(moveSquare);

                // Right click?
                if (moveSquare != 0 && view.whichButtonClicked() == MouseEvent.BUTTON2)
                {
                    board.clearSquare(moveSquare);
                    System.out.println("right");
                    break;
                }

                // Stop?
                if (view.getClicked("left") != null && view.getClicked("left").equals("arrange"))
                {
                    view.setClicked(null);
                    arranging = false;
                    movePiece = "";
                    moveSquare = 0;
                }
            }
            while (arranging && (moveSquare == 0 || movePiece.isEmpty()));

            view.setClickedSquare(0);
            
            // Destination
            if (view.whichButtonClicked() != MouseEvent.BUTTON2)
            {
                if (arranging)
                {
                    // (Highlight)
                    view.highlight(moveSquare);

                    // 2. Choose Destination
                    System.out.println("choose destination");
                    do
                    {
                        destination = view.getClickedSquare();
                    }
                    while (destination == 0);
                    view.setClickedSquare(0);


                    board.doMove(movePiece, destination); 
                }
            }
            displayAll();
        }

        System.out.println("end arrange");
    }

    public void debugLastMove()
    {
        // view.futureMoves(futureMoves, searchDepth);

        debug = true;
        chooseMove_AI(board, player);                   // do
        debug = false;

        view.setClicked(null);
    }
    ///////////////////
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters">
    public String getHistoryText()
    {
        return historyText;
    }

    public Board getBoard()
    {
        return board;
    }

    public String getPieceIDfromMove(String move)
    {
        return move.split("-")[0];
    }

    public int getDestinationFromMove(String move)
    {
        return Integer.parseInt(move.split("-")[1]);
    }

    public int getSearchDepth()
    {
        return searchDepth;
    }
    // </editor-fold>
    ///////////////////////////////////////////////////////////////////////////
}