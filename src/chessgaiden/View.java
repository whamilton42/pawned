package chessgaiden;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
import java.lang.reflect.*;
import java.text.*;
public class View implements Runnable
{
    // <editor-fold defaultstate="collapsed" desc="Attributes">
    // Game
    private Game game;
    
    // Swing
    private JFrame container;
    private JPanel panel;

    // Images
    private BufferedImage background = null;
    private BufferedImage leftArrow = null;
    private BufferedImage rightArrow = null;

    // Graphics
    private AlphaComposite alphaComp;
    private BufferStrategy strategy;

    // Text Components
    private TextArea history;

    // Checkbox
    private Checkbox debugBox;

    // Colors
    private Color darkSquares = Color.blue;

    // Fonts
    private Font textFont = new Font("Calibri", Font.PLAIN, 20);
    private FontMetrics textFont_metrics;

    private Font buttonFont = new Font("Calibri", Font.PLAIN, 20);
    private FontMetrics buttonFont_metrics;

    private Font squareNumFont = new Font("Calibri", Font.PLAIN, 40);
    private FontMetrics squareNumFont_metrics;

    // Number Formatting
    private DecimalFormat twoDP = new DecimalFormat("#0.00");

    // Sizes
    private int squareSide = 80;
    private int width = squareSide * 15;
    private int height = squareSide * 10;

    // Coordinates
    private Rectangle boardRect = new Rectangle(squareSide, squareSide, squareSide * 8, squareSide * 8);
    private Rectangle playerRect = new Rectangle(width - (squareSide / 2), squareSide * 3, (squareSide / 2), squareSide * 5);

    // Mouse
    private String leftclicked = null;
    private String rightClicked = null;
    private int clickedSquare = 0;
    private int mouseButtonClicked;

    // Buttons
    private Rectangle arrange = new Rectangle(squareSide * 10, squareSide / 2, squareSide, squareSide);
    private Rectangle debug = new Rectangle((int) (squareSide * 11.5), squareSide / 2, squareSide, squareSide);
    private Rectangle score = new Rectangle(squareSide * 13, squareSide / 2, squareSide, squareSide);
    // </editor-fold>

    // CONSTRUCTOR
    public View(Game game) throws InvocationTargetException, InterruptedException
    {
        this.game = game;

        SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    // Frame
                    container = new JFrame("Chess Gaiden");

                    // Panel
                    panel = (JPanel) container.getContentPane();
                    panel.setPreferredSize(new Dimension(width, height));
                    panel.setLayout(null);
                    panel.setBackground(Color.black);
                    panel.setIgnoreRepaint(true);


                    // Debug Box
                    debugBox = new Checkbox();
                    debugBox.setLabel("square numbers");
                    debugBox.setBackground(Color.yellow);
                    debugBox.setFont(textFont);
                    debugBox.setBounds(squareSide * 13, (int) (squareSide * 9.5), 160, 40);

                    panel.add(debugBox);


                    // History
                    history = new TextArea("", 1, 1, TextArea.SCROLLBARS_VERTICAL_ONLY );
                    history.setBounds(squareSide * 10, squareSide * 2, squareSide * 4, squareSide * 7);
                    history.setFont(textFont);
                    history.setBackground(Color.white);
                    history.setEditable(false);

                    panel.add(history);


                    // Frame
                    container.pack();
                    container.setResizable(false);
                    container.setVisible(true);
                    container.setDefaultCloseOperation(container.EXIT_ON_CLOSE);


                    // Buffer
                    container.createBufferStrategy(2);
                    strategy = container.getBufferStrategy();



                    // Font Metrics
                    squareNumFont_metrics = strategy.getDrawGraphics().getFontMetrics(squareNumFont);
                    buttonFont_metrics = strategy.getDrawGraphics().getFontMetrics(buttonFont);

                    createListeners();  // mouse
                    loadImages();       // images
                }
            }
        );
    }

    // RUN
    public void run()
    {
        //
    }
    //////////////////////////////////////////////////////////////////////////


    // Drawing
    // <editor-fold defaultstate="collapsed" desc="Drawing">
    public void drawAll(Board board, char player)
    {
        drawBackground();
        drawBoardBackground();
        drawBoard(false, board);
        drawButtons(board);
        searchDepth();
        drawPlayer(player);

        // History
        history.setVisible(false);
        history.setText(game.getHistoryText());
        history.setCaretPosition(Integer.MAX_VALUE);
        history.setVisible(true);

        strategy.show();
    }

    // Drawing : Background
    public void drawBackground()
    {
        Graphics2D g = getAdjustedGraphics();
        g.drawImage(background, 0, 0, null);
    }

    // Drawing : Board Background
    public void drawBoardBackground() {
        Graphics2D g = getAdjustedGraphics();

        g.setComposite(setAlpha(0.3f));
        g.setColor(Color.black);

        g.fillRect(boardRect.x - (squareSide / 2), boardRect.y - (squareSide / 2),
                boardRect.width + ((squareSide / 2) * 2), boardRect.height + ((squareSide / 2) * 2));
    }

    // Drawing : Board
    public void drawBoard(boolean show, Board board) {
        Graphics2D g = getAdjustedGraphics();

        // Squares
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                g.setColor(Color.white);

                if (y % 2 != 0) {
                    if (x % 2 != 0) {
                        g.setColor(darkSquares);

                    }
                } else {
                    if (x % 2 == 0) {
                        g.setColor(darkSquares);

                    }
                }

                // Colour
                g.fillRect(boardRect.x + (squareSide * x), boardRect.y + (squareSide * y), squareSide, squareSide);
            }
        }

        if (board == null) {
            return;

            // Pieces

        }
        for (int square = 1; square <= 64; square++) {
            String pieceID = board.getSquares()[square];
            if (!pieceID.isEmpty()) {
                g.setComposite(setAlpha(1f));
                g.drawImage(getGraphic(pieceID),
                        (boardRect.x + (squareSide * (Board.getFile(square) - 1))) + (squareSide / 2) - (getGraphic(pieceID).getWidth() / 2),
                        (boardRect.y + (squareSide * 7)) - (squareSide * (Board.getRank(square) - 1)) + (squareSide / 2) - (getGraphic(pieceID).getHeight() / 2),
                        null);
            }

            if (debugBox.getState() == true) {
                // Number
                g.setFont(squareNumFont);
                g.setColor(Color.black);
                g.setComposite(setAlpha(0.2f));

                g.drawString(Integer.toString(square),
                        (boardRect.x + (squareSide * (Board.getFile(square) - 1))) + (squareSide / 2) - (squareNumFont_metrics.stringWidth(Integer.toString(square)) / 2),
                        (boardRect.y + (squareSide * 7)) - (squareSide * (Board.getRank(square) - 1)) + (squareSide / 2) + (squareNumFont_metrics.getHeight() / 4));
            }
        }
        if (show) {
            strategy.show();

        }
    }

    // Drawing : Highlight
    public void highlight(int square) {
        Graphics2D g = getAdjustedGraphics();
        g.setColor(Color.red);
        g.setComposite(setAlpha(0.3f));

        g.fillRect(boardRect.x + (squareSide * (Board.getFile(square) - 1)), (boardRect.y + (squareSide * 7)) - (squareSide * (Board.getRank(square) - 1)), squareSide, squareSide);

        strategy.show();
    }

    // Drawing : Buttons - All
    public void drawButtons(Board board) {
        drawButton(arrange, "arrange", "Arrange", Color.green);
        drawButton(debug, "debug", "Debug", Color.red);
        drawButton(score, "score", twoDP.format(board.evaluate()), Color.yellow);
    }

    // Drawing : Buttons - Specific
    public void drawButton(Rectangle rect, String name, String displayName, Color color) {
        Graphics2D g = getAdjustedGraphics();

        // Box
        g.setColor(color);
        g.setComposite(setAlpha(0.5f));
        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        // Text
        g.setColor(Color.white);
        g.setComposite(setAlpha(1f));
        g.setFont(buttonFont);
        g.drawString(displayName, rect.x + (squareSide / 2) - (buttonFont_metrics.stringWidth(displayName) / 2), (rect.y + (rect.height / 2)) + ((int) buttonFont_metrics.getHeight() / 4));
    }

    // Drawing : Player
    public void drawPlayer(char player)
    {
        Graphics2D g = getAdjustedGraphics();

        // Box
        if (player == 'w')
            g.setColor(Color.white);
        else
            g.setColor(Color.black);
        
        g.setComposite(setAlpha(0.8f));
        g.fillRect(playerRect.x, playerRect.y, playerRect.width, playerRect.height);
    }

    // Drawing : Future Moves
    public void futureMoves(Board[] futureMoves, int searchDepth)
    {
        Graphics2D g = getAdjustedGraphics();

        // Arrows
        //g.drawImage(leftArrow, boardX - squareSide - leftArrow.getWidth(), boardY + (squareSide * 4) - (leftArrow.getHeight() / 2), null);
        //g.drawImage(rightArrow, boardX + (squareSide * 8) + rightArrow.getWidth(), boardY + (squareSide * 4) - (rightArrow.getHeight() / 2), null);

        for (int depth = 0; depth <= searchDepth; depth++) {
            drawBackground();
            drawBoardBackground();

            // Title : Box
            g.setComposite(setAlpha(0.3f));
            g.setColor(Color.black);
            g.fillRect(0, 0, width, squareSide);

            // Title : Text
            g.setComposite(setAlpha(1f));
            g.setColor(Color.white);
            g.setFont(squareNumFont);
            g.drawString("The Plan" + "     #" + Integer.toString(depth), squareSide + (squareSide * 4) - squareNumFont_metrics.stringWidth("The Plan") / 2, squareSide / 2);


            // Board
            drawBoard(true, futureMoves[depth]);

            try {
                Thread.sleep(3000);
            } catch (Exception meh) {
            }
        }
    }

    // Drawing : Checkmate
    public void drawCheckmate()
    {
        Graphics2D g = getAdjustedGraphics();

        // Title : Box
        g.setComposite(setAlpha(0.3f));
        g.setColor(Color.black);
        g.fillRect(0, 0, width, squareSide);

        // Title : Text
        g.setComposite(setAlpha(1f));
        g.setColor(Color.white);
        g.setFont(squareNumFont);
        g.drawString("Checkmate", squareSide + (squareSide * 4) - squareNumFont_metrics.stringWidth("Checkmate") / 2, squareSide / 2);

        strategy.show();
    }

    // Drawing : Search Depth
    public void searchDepth() {
        Graphics2D g = getAdjustedGraphics();

        // Box
        g.setColor(Color.white);
        g.setComposite(setAlpha(1f));
        g.setFont(squareNumFont);

        String string = "Search: " + Integer.toString(game.getSearchDepth()) + " Ply";

        //g.drawString(string, width - squareSide - squareNumFont_metrics.stringWidth(string), squareSide);
    }
    ///////////////////////////
    // </editor-fold>



    // Mouse
    // <editor-fold defaultstate="collapsed" desc="Mouse">
    ///////////////////////////
    // Mouse : Listeners
    public void createListeners()
    {
        // Board
        for (int square = 1; square <= 64; square++)
        {
            panel.addMouseListener(new RectListener(this,
                    Integer.toString(square),
                    (boardRect.x + (squareSide *  (Board.getFile(square) - 1))),
                    (boardRect.y + (squareSide * 7)) - (squareSide *  (Board.getRank(square) - 1)),
                    squareSide));
        }

        // Buttons
        panel.addMouseListener(new RectListener(this, "arrange", arrange));
        panel.addMouseListener(new RectListener(this, "debug", debug));
    }


    // Mouse : Click?
    public void setClicked(String square)
    {
        leftclicked = square;
    }


    
    // Mouse : Get Clicked - Button
    public String getClicked(String mouseButton)
    {
        if (mouseButton.equals("left"))
            return leftclicked;
        else
            return rightClicked;
    }

    // Mouse : Get Clicked - Square
    public int getClickedSquare()
    {
        return clickedSquare;
    }


    // Mouse : Square Click?
    public void setClickedSquare(int clickedSquare)
    {
        this.clickedSquare = clickedSquare;
    }

    // Mouse : Which Button?
    public int whichButtonClicked()
    {
        return mouseButtonClicked;
    }

    //
    public void setButtonClicked(int mouseButtonClicked)
    {
        this.mouseButtonClicked = mouseButtonClicked;
    }


    ///////////////////////////
    // </editor-fold>


    
    ///////////////////////////
    // Images
    public void loadImages()
    {
        try
        {
            background = ImageIO.read(new File("images/background.png"));
            leftArrow = ImageIO.read(new File("images/leftArrow.png"));
            rightArrow = ImageIO.read(new File("images/rightArrow.png"));
        }
        catch (IOException e)
        {
            System.out.println("Image file not found!");
        }
    }

    public BufferedImage getGraphic(String pieceID)
    {
        String graphicAddress = "";

        switch(pieceID.charAt(1))
        {
            case ('K'):
                graphicAddress = "King.png";
                break;
            case ('Q'):
                graphicAddress = "Queen.png";
                break;
            case ('C'):
                graphicAddress = "Castle.png";
                break;
            case ('B'):
                graphicAddress = "Bishop.png";
                break;
            case ('N'):
                graphicAddress = "Knight.png";
                break;
            case ('P'):
                graphicAddress = "Pawn.png";
                break;
        }


        try
        {
            return ImageIO.read(new File("images/pieces/" + pieceID.charAt(0) + graphicAddress));
        }
        catch (Exception meh) { }

        return null;
    }
    ///////////////////////////


    ///////////////////////////
    // Graphics
    public Graphics2D getAdjustedGraphics()
    {
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        g.translate(container.getInsets().left, container.getInsets().top);

        return g;
    }

    public AlphaComposite setAlpha(float alpha)
    {
        alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        return alphaComp;
    }
    ///////////////////////////
}