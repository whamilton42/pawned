package chessgaiden;
// This static class generates a list of all potential moves, for all pieces,
// from every square to every other square.
public final class Moves
{
    // ATTRIBUTES
    private static final boolean[][] kingMoves = new boolean[65][65];
    private static final boolean[][] queenMoves = new boolean[65][65];
    private static final boolean[][] castleMoves = new boolean[65][65];
    private static final boolean[][] bishopMoves = new boolean[65][65];
    private static final boolean[][] knightMoves = new boolean[65][65];
    
    private static final boolean[][] whitePawnMoves = new boolean[65][65];
    private static final boolean[][] blackPawnMoves = new boolean[65][65];


    // CONSTRUCTOR
    public Moves()
    {
        generateMoves();
    }
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    // Generate
    public static void generateMoves()
    {
        // For all moves..
        for (int current = 1; current <= 64; current++)
        {
            for (int destination = 1; destination <= 64; destination++)
            {
                // King
                if (generateValidTheoretically("wK", current, destination))
                {
                    kingMoves[current][destination] = true;
                }

                // Queen
                if (generateValidTheoretically("wQ", current, destination))
                {
                    queenMoves[current][destination] = true;
                }

                // Knight
                if (generateValidTheoretically("wN", current, destination))
                {
                    knightMoves[current][destination] = true;
                }

                // Bishop
                if (generateValidTheoretically("wB", current, destination))
                {
                    bishopMoves[current][destination] = true;
                }

                // Castle
                if (generateValidTheoretically("wC", current, destination))
                {
                    castleMoves[current][destination] = true;

                }

                // Pawn : White
                if (generateValidTheoretically("wP", current, destination))
                {
                    whitePawnMoves[current][destination] = true;
                }

                // Pawn : Black
                if (generateValidTheoretically("bP", current, destination))
                {
                    blackPawnMoves[current][destination] = true;
                }
            }
        }
    }

    public static boolean generateValidTheoretically(String pieceID, int current, int destination)
    {
        // Goin' nowhere?
        if (current == destination)
            return false;


        switch (pieceID.charAt(1))
        {
            // <editor-fold defaultstate="collapsed" desc="King">
            case (Board.KING):
                // Castling!

                // One Away
                if (    Math.abs(current - destination) == 1
                        ||
                        Math.abs(current - destination) == 7
                        ||
                        Math.abs(current - destination) == 8
                        ||
                        Math.abs(current - destination) == 9
                        ||
                        Math.abs(current - destination) == 1)
                    return true;


                break;
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Queen">
            case Board.QUEEN:
            {
                // Horizontal
                if (Board.getRank(current) == Board.getRank(destination))
                    return true;

                // Vertical
                if (Board.getFile(current) == Board.getFile(destination))
                    return true;

                // Diagonal
                if ( Math.abs(Board.getFile(current) - Board.getFile(destination)) - Math.abs(Board.getRank(current) - Board.getRank(destination)) == 0)
                    return true;

                 break;
            }

            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Castle">
            case Board.CASTLE:
            {
                // Horizontal
                if (Board.getRank(current) == Board.getRank(destination))
                    return true;

                // Vertical
                if (Board.getFile(current) == Board.getFile(destination))
                    return true;

                break;
            }
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Bishop">
            case Board.BISHOP:
            {
                // Diagonal
                if ( Math.abs(Board.getFile(current) - Board.getFile(destination)) - Math.abs(Board.getRank(current) - Board.getRank(destination)) == 0)
                    return true;

                break;
            }
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Knight">
            case Board.KNIGHT:
            {
                if (Math.abs(Board.getRank(current) - Board.getRank(destination)) == 2)
                {
                    if (Math.abs(Board.getFile(current) - Board.getFile(destination)) == 1)
                        return true;
                }

                if (Math.abs(Board.getRank(current) - Board.getRank(destination)) == 1)
                {
                    if (Math.abs(Board.getFile(current) - Board.getFile(destination)) == 2)
                        return true;
                }

                break;
            }
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Pawn">
            case (Board.PAWN):
            {
                if (pieceID.charAt(0) == Board.WHITE)
                {
                    // Vertical
                    if (Board.getFile(destination) == Board.getFile(current))
                    {
                        // One up.
                        if (Board.getRank(destination) - Board.getRank(current) == 1)
                            return true;

                        // Two up.
                        if (Board.getRank(current) == 2 && Board.getRank(destination) == 4)
                                return true;
                    }

                    // Diagonal
                        if ((Board.getRank(destination) - Board.getRank(current) == 1)
                            &&
                            Math.abs(Board.getFile(destination) - Board.getFile(current)) == 1)
                                    return true;
                }
                else
                {
                    // Vertical
                    if (Board.getFile(destination) == Board.getFile(current))
                    {
                        // One down.
                        if (Board.getRank(destination) - Board.getRank(current) == -1)
                            return true;

                        // Two down.
                        if (Board.getRank(current) == 7 && Board.getRank(destination) == 5)
                                return true;
                    }

                    // Diagonal
                    if ((Board.getRank(destination) - Board.getRank(current) == -1)
                        &&
                        Math.abs(Board.getFile(destination) - Board.getFile(current)) == 1)
                                return true;
                }

                break;
            }
            // </editor-fold >
        }

        return false;
    }
    ///////////////////////////////////////////////////////////////////////////
    

    ///////////////////////////////////////////////////////////////////////////
    // Query
    public static boolean validTheoretically(String pieceID, int current, int destination)
    {
        switch (pieceID.charAt(1))
        {
            case (Board.KING):
                return (kingMoves[current][destination]);
            case (Board.QUEEN):
                return (queenMoves[current][destination]);
            case (Board.CASTLE):
                return (castleMoves[current][destination]);
            case (Board.KNIGHT):
                return (knightMoves[current][destination]);
            case (Board.BISHOP):
                return (bishopMoves[current][destination]);
            case (Board.PAWN):
            {
                if (pieceID.charAt(0) == Board.WHITE)
                    return (whitePawnMoves[current][destination]);
                else
                    return (blackPawnMoves[current][destination]);
            }
        }

        return false;
    }
    ///////////////////////////////////////////////////////////////////////////
}