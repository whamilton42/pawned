package chessgaiden;
// This class models a chess board. It deals with detailed queries about the
// state of the board and can produce potential moves etc.
public class Board
{
    // <editor-fold defaultstate="collapsed" desc="Attributes">
    private String[] squares;
    private int queenCount_white = 1;   // for promotion
    private int queenCount_black = 1;   // for promotion

    // Constants
    public static final char PAWN = 'P';
    public static final char KING = 'K';
    public static final char QUEEN = 'Q';
    public static final char CASTLE = 'C';
    public static final char BISHOP = 'B';
    public static final char KNIGHT = 'N';

    public static final int PAWN_VALUE = 1;
    public static final int KING_VALUE = 10000;
    public static final int QUEEN_VALUE = 9;
    public static final int CASTLE_VALUE = 5;
    public static final int BISHOP_VALUE = 3;
    public static final int KNIGHT_VALUE = 3;
    
    public static final char WHITE = 'w';
    public static final char BLACK = 'b';
    // </editor-fold>

    ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public Board()
    {
        squares = new String[65];

        // Whites
        squares[9]  = "" + WHITE + PAWN + "1";
        squares[10] = "" + WHITE + PAWN + "2";
        squares[11] = "" + WHITE + PAWN + "3";
        squares[12] = "" + WHITE + PAWN + "4";
        squares[13] = "" + WHITE + PAWN + "5";
        squares[14] = "" + WHITE + PAWN + "6";
        squares[15] = "" + WHITE + PAWN + "7";
        squares[16] = "" + WHITE + PAWN + "8";

        squares[1] = "" + WHITE + CASTLE + "1";
        squares[2] = "" + WHITE + KNIGHT + "1";
        squares[3] = "" + WHITE + BISHOP + "1";
        squares[4] = "" + WHITE + QUEEN  + "1";
        squares[5] = "" + WHITE + KING   + "1";
        squares[6] = "" + WHITE + BISHOP + "2";
        squares[7] = "" + WHITE + KNIGHT + "2";
        squares[8] = "" + WHITE + CASTLE + "2";

        // Empties
        for (int square = 17; square <= 48; square++)
            squares[square] = "";

        // Blacks
        squares[49] = "" + BLACK + PAWN + "1";
        squares[50] = "" + BLACK + PAWN + "2";
        squares[51] = "" + BLACK + PAWN + "3";
        squares[52] = "" + BLACK + PAWN + "4";
        squares[53] = "" + BLACK + PAWN + "5";
        squares[54] = "" + BLACK + PAWN + "6";
        squares[55] = "" + BLACK + PAWN + "7";
        squares[56] = "" + BLACK + PAWN + "8";

        squares[57] = "" + BLACK + CASTLE + "1";
        squares[58] = "" + BLACK + KNIGHT + "1";
        squares[59] = "" + BLACK + BISHOP + "1";
        squares[60] = "" + BLACK + QUEEN  + "1";
        squares[61] = "" + BLACK + KING   + "1";
        squares[62] = "" + BLACK + BISHOP + "2";
        squares[63] = "" + BLACK + KNIGHT + "2";
        squares[64] = "" + BLACK + CASTLE + "2";
    }

    // CONSTRUCTOR : Copy
    public Board(Board copy)
    {
        squares = new String[65];

        for (int square = 1; square <= 64; square++)
        {
            if (!copy.getSquares()[square].isEmpty())
                squares[square] = copy.getSquares()[square];
            else
                squares[square] = "";
        }
    }
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    // Potential Moves
    // Note - this relies on moveOkay, not just Moves.validTheoretically!
    public String[] potentialMoves(char player)
    {
        // Checkmate? Stop!
        if (isCheckmate())
            return null;

        //////////////////////////////////////////////////////////////////////
        // Initialise
        String[] potentialMoves = new String[432];     // 16 x 63
        int count = 0;

        // For every square..
        for (int square = 1; square <= 64; square++)
        {
            // Fetch the piece..
            String pieceID = squares[square];

            // If it's the player's..
            if (!pieceID.isEmpty() && playerFromID(pieceID) == player)
            {
                // For every square..
                for (int destination = 1; destination <= 64; destination++)
                {
                    // Add the move if okay.
                    if (moveOkay(pieceID, destination))
                    {
                        potentialMoves[count] = pieceID + "-" + destination;
                        count++;
                    }
                }
            }
        }

        return potentialMoves;
    }
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    // Move Okay ?
    public boolean moveOkay(String pieceID, int destination)
    {
        //System.out.println("move okay? : " + pieceID + " to " + destination);

        // 0. Blank?
        if (destination == 99)
        {
            System.out.println("blank move..?");
            System.exit(5);
        }


        // Save
        int originalPosition = getPosition(pieceID);
        String destinationPiece = "";
        if (!squares[destination].isEmpty())
            destinationPiece = whatHere(destination);


        ///////////////////////////////////////////////////////////////////////
        // 1. Ally at the destination?
        if (!destinationPiece.isEmpty() && destinationPiece.charAt(0) == playerFromID(pieceID))
        {
            //System.out.println("- ally there");
            return false;
        }

        // 2. Not theoretically okay?
        if (!validTheoretically(pieceID, destination))
        {
            //System.out.println("- theoretically invalid");
            return false;
        }

        // 2a. Pawn..?
        if (pieceTypeFromID(pieceID) == PAWN)
        {
            // Diagonal, but no enemy..
            if (getFile(getPosition(pieceID)) != getFile(destination))
            {
                // Nothing there
                if (whatHere(destination).isEmpty())
                {
                    return false;
                }
            }
            // Vertical, but occupied..
            else
            {
                // Something there
                if (!whatHere(destination).isEmpty())
                {
                    return false;
                }
            }
        }

        // 3. Blocked?
        if (isBlocked(pieceID, destination))
        {
            //System.out.println("- blocked");
            return false;
        }

        // 4. Check?
        // a) do the move.
        doMove(pieceID, destination);

        // b) check for check
        boolean wouldCauseCheck = inCheck(playerFromID(pieceID));

        // c) put pieces back..
        placePiece(pieceID, originalPosition);
        placePiece(destinationPiece, destination);

        if (wouldCauseCheck)
        {
            //System.out.println(pieceID + " to " + destination + " causes check for " + playerFromID(pieceID));
            return false;
        }
        else
        {
            //System.out.println(pieceID + " to " + destination + " doesn't cause check for " + playerFromID(pieceID));
        }
        ///////////////////////////////////////////////////////////////////////

        return true;
    }
   
    public boolean validTheoretically(String pieceID, int destination)
    {
        if (Moves.validTheoretically(pieceID, getPosition(pieceID), destination))
            return true;
        else
            return false;
    }

    public boolean isBlocked(String pieceID, int destination)
    {
        // Knight?
        if (pieceTypeFromID(pieceID) == KNIGHT)
            return false;


        int current = getPosition(pieceID);

        // Vertical
        if (getFile(current) == getFile(destination))
        {
            // Upwards
            if (getRank(current) < getRank(destination))
            {
                for (int square = current + 8; square < destination; square += 8)
                {
                    if (!squares[square].isEmpty())
                        return true;
                }
            }
            // Downwards
            else
            {
                for (int square = current - 8; square > destination; square -= 8)
                {
                    if (!squares[square].isEmpty())
                        return true;
                }
            }
        }

        // Horizontal
        if (getRank(current) == getRank(destination))
        {
            // Rightwards
            if (getFile(current) < getFile(destination))
            {
                for (int square = current + 1; square < destination; square++)
                {
                    if (!squares[square].isEmpty())
                        return true;
                }
            }
            // Leftwards
            else
            {
                for (int square = current - 1; square > destination; square--)
                {
                    if (!squares[square].isEmpty())
                        return true;
                }
            }
        }

        // Diagonal
        if ( Math.abs(getFile(current) - getFile(destination)) - Math.abs(getRank(current) - getRank(destination)) == 0)
        {
            // Upwards
            if (getRank(current) < getRank(destination))
            {
                // Rightwards
                if (getFile(current) < getFile(destination))
                {
                    for (int square = current + 9; square != destination; square += 9)
                    {
                        if (!squares[square].isEmpty())
                            return true;
                    }
                }
                // Leftwards
                else
                {
                    for (int square = current + 7; square != destination; square += 7)
                    {
                        if (!squares[square].isEmpty())
                            return true;
                    }
                }
            }
            else
            // Downwards
            {
                // Rightwards
                if (getFile(current) < getFile(destination))
                {
                    for (int square = current - 7; square != destination; square -= 7)
                    {
                        if (!squares[square].isEmpty())
                            return true;
                    }
                }
                // Leftwards
                else
                {
                    for (int square = current - 9; square != destination; square -= 9)
                    {
                        if (!squares[square].isEmpty())
                            return true;
                    }
                }
            }

        }

        return false;
    }


    // Necessary, otherwise the AI will 'escape' check simply by causing
    // check to the opponent - which obviously shouldn't count.
    public boolean moveOkay_disregardingCheck(String pieceID, int destination)
    {
        //System.out.println("move okay? : " + pieceID + " to " + destination);

        // 0. Blank?
        if (destination == 99)
        {
            System.out.println("blank move wut");
            System.exit(5);
        }

        // Save
        String destinationPiece = "";
        if (!squares[destination].isEmpty())
            destinationPiece = whatHere(destination);


        ///////////////////////////////////////////////////////////////////////
        // 1. Ally at the destination?
        if (!destinationPiece.isEmpty() && destinationPiece.charAt(0) == playerFromID(pieceID))
        {
            //System.out.println("- ally there");
            return false;
        }

        // 2. Not theoretically okay?
        if (!validTheoretically(pieceID, destination))
        {
            //System.out.println("- theoretically invalid");
            return false;
        }

        // 2a. Pawn..?
        if (pieceTypeFromID(pieceID) == PAWN)
        {
            // Diagonal, but no enemy..
            if (getFile(getPosition(pieceID)) != getFile(destination))
            {
                // Nothing there
                if (whatHere(destination).isEmpty())
                {
                    return false;
                }
            }
            // Vertical, but occupied..
            else
            {
                // Something there
                if (!whatHere(destination).isEmpty())
                {
                    return false;
                }
            }
        }

        // 3. Blocked?
        if (isBlocked(pieceID, destination))
        {
            //System.out.println("- blocked");
            return false;
        }


        return true;
    }
    ///////////////////////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////////////////////
    // Check & Checkmate
    public boolean inCheck(char player)
    {
        // King
        int kingSquare = 0;
        switch (player)
        {
            case WHITE:
                kingSquare = getPosition("wK1");
                break;
            case BLACK:
                kingSquare = getPosition("bK1");
                break;
        }

        if (kingSquare == 100)
        {
            return false;
        }
        else
        {
            // For every square..
            for (int square = 1; square <= 64; square++)
            {
                // If a piece can get the king, it's check.
                if (!squares[square].isEmpty()
                        && squares[square].charAt(0) == Game.flipPlayer(player)
                        && moveOkay_disregardingCheck(whatHere(square), kingSquare))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isCheckmate()
    {
        if (isCheckmate(WHITE))
            return true;
        if (isCheckmate(BLACK))
            return true;

        return false;
    }

    public boolean isCheckmate(char player)
    {
        if (!inCheck(player))
            return false;

        // If the player can't move, it's checkmate.
        for (int square = 1; square <= 64; square++)
        {
            if (!squares[square].isEmpty()
                    && playerFromID(squares[square]) == player
                    && canMove(squares[square]))
                return false;
        }

        return true;
    }
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    // Move
    public void doMove(String pieceID, int destination)
    {
        int originalSquare = getPosition(pieceID);

        squares[destination] = pieceID;
        squares[originalSquare] = "";


        // Promotion - White
        if (getRank(destination) == 8 && playerFromID(pieceID) == WHITE && pieceTypeFromID(pieceID) == PAWN)
        {
            placePiece("" + WHITE + QUEEN + queenCount_white + 1, destination);
            queenCount_white++;
        }

        // Promotion - Black
        if (getRank(destination) == 1 && playerFromID(pieceID) == BLACK && pieceTypeFromID(pieceID) == PAWN)
        {
            placePiece("" + BLACK + QUEEN + queenCount_black + 1, destination);
            queenCount_black++;
        }
    }

    public void placePiece(String pieceID, int square)
    {
        squares[square] = pieceID;
    }

    public void clearSquare(int square)
    {
        squares[square] = "";
    }
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    // Display
    public void display(String indent)
    {
        System.out.println(indent + "   a \u200A\u200Ab \u200Ac \u200A\u200A\u200Ad \u200A\u200A\u200Ae \u200A\u200A\u200Af \u200A\u200A\u200A\u200Ag \u200A\u200A\u200Ah");
        // Loop through the entire board. This order puts a1 at the bottom-left.
        for (int y = 8; y >= 1; y--)
        {
            System.out.print(indent + String.valueOf(y) + " ");
            for (int x = 1; x <= 8; x++)
            {
                String pieceID = whatHere(x, y);
                if (!pieceID.isEmpty())
                    System.out.print("\u200A\u200A\u200A\u200A\u200A\u200A\u200A" + getUnicode(pieceID));
                else
                    System.out.print("\u200A\u200A\u200A\u200A\u200A\u200A\u200A\u200A\u200A\u200A\u25A1\u200A");
            }
            System.out.print(" " + String.valueOf(y));


            System.out.print(indent + "\t");

            System.out.println();
        }
       System.out.println(indent + "   a \u200A\u200Ab \u200Ac \u200A\u200A\u200Ad \u200A\u200A\u200Ae \u200A\u200A\u200Af \u200A\u200A\u200A\u200Ag \u200A\u200A\u200Ah");
    }

    public void display(String indent, Board board)
    {
        System.out.println(indent + "   a \u200A\u200Ab \u200Ac \u200A\u200A\u200Ad \u200A\u200A\u200Ae \u200A\u200A\u200Af \u200A\u200A\u200A\u200Ag \u200A\u200A\u200Ah");
        // Loop through the entire board. This order puts a1 at the bottom-left.
        for (int y = 8; y >= 1; y--)
        {
            System.out.print(indent + String.valueOf(y) + " ");
            for (int x = 1; x <= 8; x++)
            {
                String pieceID = board.whatHere(x, y);
                if (!pieceID.isEmpty())
                    System.out.print("\u200A\u200A\u200A\u200A\u200A\u200A\u200A" + getUnicode(pieceID));
                else
                    System.out.print("\u200A\u200A\u200A\u200A\u200A\u200A\u200A\u200A\u200A\u200A\u25A1\u200A");
            }
            System.out.print(" " + String.valueOf(y));


            System.out.print(indent + "\t");

            System.out.println();
        }
       System.out.println(indent + "   a \u200A\u200Ab \u200Ac \u200A\u200A\u200Ad \u200A\u200A\u200Ae \u200A\u200A\u200Af \u200A\u200A\u200A\u200Ag \u200A\u200A\u200Ah");
    }

    public String getUnicode(String pieceID)
    {
        if (playerFromID(pieceID) == WHITE)
        {
            switch(pieceTypeFromID(pieceID))
            {
                case (KING):
                    return "\u2654";
                case (QUEEN):
                    return "\u2655";
                case (CASTLE):
                    return "\u2656";
                case (BISHOP):
                    return "\u2657";
                case (KNIGHT):
                    return "\u2658";
                case (PAWN):
                    return "\u2659";
            }
        }
        else
        {
            switch(pieceTypeFromID(pieceID))
            {
                case (KING):
                    return "\u265A";
                case (QUEEN):
                    return "\u265B";
                case (CASTLE):
                    return "\u265C";
                case (BISHOP):
                    return "\u265D";
                case (KNIGHT):
                    return "\u265E";
                case (PAWN):
                    return "\u265F";
            }
        }

        return "unicode error";
    }
    ///////////////////////////////////////////////////////////////////////////

    
    ///////////////////////////////////////////////////////////////////////////
    // Heuristics
    public double evaluate()
    {
        // Checkmate
        if (isCheckmate(WHITE))
            return -10000;
        if (isCheckmate(BLACK))
            return 10000;

        int whiteMateriel = 0, blackMateriel = 0;

        // Materiel
        // For every square..
        for (int square = 1; square <= 64; square++)
        {
            if (!squares[square].isEmpty())
            {
                if (playerFromID(squares[square]) == WHITE)
                    whiteMateriel += pieceValue(squares[square]);
                else
                    blackMateriel += pieceValue(squares[square]);
            }
        }

        int materiel = whiteMateriel - blackMateriel;

        // Options
        //double potentialMoves = (0.1 * howManyMoves(WHITE)) - (0.1 * howManyMoves(BLACK));
        //double theoreticalMoves = (0.1 * howManyTheoreticalMoves(WHITE)) - (0.1 * howManyTheoreticalMoves(BLACK));

        
        return materiel;// + theoreticalMoves;
    }

    public int howManyMoves(char player)
    {
        String[] moves =potentialMoves(player);
        int count = 0;

        for (int move = 0; move < moves.length; move++)
        {
            if (moves[move] != null)
                count++;
        }

        return count;
    }

    public int howManyTheoreticalMoves(char player)
    {
        // Checkmate? Stop!
        if (isCheckmate())
            return 0;

        int count = 0;

        //////////////////////////////////////////////////////////////////////
        // For every square..
        for (int square = 1; square <= 64; square++)
        {
            // Fetch the piece..
            String pieceID = squares[square];

            // If it's the player's..
            if (!pieceID.isEmpty() && playerFromID(pieceID) == player)
            {
                // For every square..
                for (int destination = 1; destination <= 64; destination++)
                {
                    // Add the move if okay.
                    if (validTheoretically(pieceID, destination))
                    {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    public boolean canMove(String pieceID)
    {
        // For every square..
        for (int square = 1; square <= 64; square++)
        {
            if (moveOkay(pieceID, square))
                return true;
        }

        return false;
    }
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    // Utilities 
    public static int getFile(int square)
    {
        if (square % 8 == 0)
            return 8;
        else
            return square % 8;
    }

    public static int getRank(int square)
    {
        double squareDouble = square;

        return (int) (Math.ceil(squareDouble / 8));
    }

    public static char playerFromID(String pieceID)
    {
        return pieceID.charAt(0);
    }

    public static char pieceTypeFromID(String pieceID)
    {
        return pieceID.charAt(1);
    }
    
    public static int pieceValue(String pieceID)
    {
        switch (pieceTypeFromID(pieceID))
        {
            case (KING):
                return KING_VALUE;
            case (QUEEN):
                return QUEEN_VALUE;
            case (BISHOP):
                return BISHOP_VALUE;
            case (KNIGHT):
                return KNIGHT_VALUE;
            case (CASTLE):
                return CASTLE_VALUE;
            case (PAWN):
                return PAWN_VALUE;
        }

        System.out.println("Cannot parse value from " + pieceID);
        return 0;
    }
    ///////////////////////////////////////////////////////////////////////////

    
    ///////////////////////////////////////////////////////////////////////////
    // Getters
    public String[] getSquares()
    {
        return squares;
    }

    public String whatHere(int square)
    {
        return squares[square];
    }

    public String whatHere(int x, int y)
    {
        int squareNum = x + ((y - 1) * 8);

        if (squareNum < 1 || squareNum > 64) {
            return null;

        } else {
            return squares[x + ((y - 1) * 8)];

        }
    }

    public int getPosition(String pieceID)
    {
        // For every square..
        for (int square = 1; square <= 64; square++)
        {
            if (!squares[square].isEmpty() && squares[square].equals(pieceID))
            {
                return square;

            }
        }

        // display("");
        //System.out.println("Querying the position of a piece that does not exist - " + pieceID);

        return 100;
    }
    ///////////////////////////////////////////////////////////////////////////
}