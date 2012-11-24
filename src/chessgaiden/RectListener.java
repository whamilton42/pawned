package chessgaiden;
import java.awt.event.*;
import java.awt.*;
// This class defines a MouseListener with a specific name. It updates the View
// class whenever a MouseListener is clicked on, telling View the name.
public class RectListener implements MouseListener
{
    // ATTRIBUTES
    private Rectangle rectangle;
    private String square;
    private View graphics;

    
    // CONSTRUCTOR : Square
    public RectListener(View graphics, String square, int x, int y, int squareSide)
    {
        this.graphics = graphics;
        this.square = square;
        rectangle = new Rectangle(x, y, squareSide, squareSide);
    }

    // CONSTRUCTOR : Rectangle
    public RectListener(View graphics, String square, Rectangle rectangle)
    {
        this.graphics = graphics;
        this.square = square;
        this.rectangle = rectangle;
    }
    //////////////////////////////////////////////////////////////////////////


    // Getters
    public String getSquare()
    {
        return square;
    }


    // Mouse : Click
    public void mouseReleased(MouseEvent e)
    {
        if(rectangle.contains(e.getX(), e.getY()))
        {
            try
            {
                graphics.setClickedSquare(Integer.parseInt(square));
            }
            catch (NumberFormatException ex)
            {
                graphics.setClicked(square);
            }

            graphics.setButtonClicked(e.getButton());  
        }
    }

    // Mouse : Unused
    public void mousePressed(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
//////////////////////////////////////////////////////////////////////////