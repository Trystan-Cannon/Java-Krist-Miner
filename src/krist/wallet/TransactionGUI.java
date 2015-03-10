package krist.wallet;

import gui.ManagerGUI;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

/**
 * The user interface for making Krist transactions. This window is spawned
 * alongside the main miner interface from the wallet menu.
 * 
 * @author Trystan Cannon
 */
public class TransactionGUI extends JFrame implements WindowListener
{
    public static final int WINDOW_WIDTH  = 300;
    public static final int WINDOW_HEIGHT = 400;
    
    public static final String ACTION_COMMAND = "wallet.menu.transactionWindow";
    
    private boolean isOpen;
    private final ManagerGUI gui;
    
    /**
     * Creates the user interface with a notable characteristic:
     *  - The frame's default close operation is hiding, meaning it must be disposed of manually.
     * 
     * @param gui The <code>ManagerGUI</code> object from which this window was spawned.
     * @param x The x position at which the frame will spawn.
     * @param y The y position at which the frame will spawn.
     */
    public TransactionGUI (ManagerGUI gui, int x, int y)
    {
        // Establish the frame's basic characteristics.
        super ("Transactions");
        setResizable (false);
        setSize (WINDOW_WIDTH, WINDOW_HEIGHT);
        setLayout (new FlowLayout (FlowLayout.LEADING, 30, 10));
        setLocation (x, y);
        // Ensure that the frame is disposed instead of closing the program.
        setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
        
        this.gui = gui;
        isOpen   = true;
        
        addWindowListener (this);
        setVisible (true);
    }
    
    @Override
    /**
     * Informs the <code>ManagerGUI</code> instance that this transaction window
     * has been closed and needs to be disposed of.
     */
    public void windowClosing(WindowEvent windowEvent)
    {
        isOpen = false;
        gui.closeWindow (ACTION_COMMAND);
    }
    
    @Override
    public void windowOpened(WindowEvent e)
    {
    }

    @Override
    public void windowClosed(WindowEvent e)
    {
    }

    @Override
    public void windowIconified(WindowEvent e)
    {
    }

    @Override
    public void windowDeiconified(WindowEvent e)
    {
    }

    @Override
    public void windowActivated(WindowEvent e)
    {
    }

    @Override
    public void windowDeactivated(WindowEvent e)
    {
    }
}
