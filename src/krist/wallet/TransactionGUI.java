package krist.wallet;

import gui.ManagerGUI;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import krist.miner.Utils;

/**
 * The user interface for making Krist transactions. This window is spawned
 * alongside the main miner interface from the wallet menu.
 * 
 * @author Trystan Cannon
 */
public class TransactionGUI extends JFrame implements WindowListener, ActionListener
{
    public static final int WINDOW_WIDTH  = 265;
    public static final int WINDOW_HEIGHT = 180;
    
    /**
     * The width of every field contained within a <code>TransactionGUI</code>
     * frame.
     */
    public static final int FIELD_WIDTH = 21;
    
    /**
     * The action command distributed to the ManagerGUI, an <code>ActionListener</code>
     * when the user wants to open the transaction window.
     */
    public static final String ACTION_COMMAND = "wallet.menu.transactionWindow";
    
    /**
     * Field in which the user inputs the address to which they would like
     * to send KST.
     */
    private final JTextField recipientField;
    
    /**
     * Field in which the user inputs how much KST they would like to the
     * recipient address.
     */
    private final JTextField amountField;
    
    /**
     * The button to make the transaction specified by the user's input in
     * the recipient and amount fields.
     */
    private final JButton sendKristButton;
    
    /**
     * The button to close the transaction window. This is functionally
     * unnecessary, but it fills up the space, making the frame look
     * more symmetrical.
     */
    private final JButton cancelTransactionButton;
    
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
        setLayout (new FlowLayout (FlowLayout.LEADING, 10, 10));
        setLocation (x, y);
        // Ensure that the frame is disposed instead of closing the program.
        setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
        
        this.gui = gui;
        isOpen   = true;
        
        /**
         * Initialize all fields to use a titled border and have a width
         * of <code>TransactionGUI.FIELD_WIDTH</code>.
         */
        recipientField = new JTextField(FIELD_WIDTH);
        recipientField.setBorder(BorderFactory.createTitledBorder("Recipient Address"));
        
        amountField = new JTextField(FIELD_WIDTH);
        amountField.setBorder(BorderFactory.createTitledBorder("Amount (KST)"));
        
        sendKristButton = new JButton("Send KST");
        sendKristButton.setActionCommand("transaction.send");
        sendKristButton.addActionListener(this);
        
        cancelTransactionButton = new JButton("Cancel Transaction");
        cancelTransactionButton.setActionCommand("transaction.cancel");
        cancelTransactionButton.addActionListener(this);
        
        /**
         * Add all components to the frame.
         */
        add(recipientField);
        add(amountField);
        add(sendKristButton);
        add(cancelTransactionButton);
        
        addWindowListener(this);
        setVisible(true);
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
    /**
     * Handles the user's actions when they are interacting with the transaction
     * window, i.e button clicks, etc.
     */
    public void actionPerformed(ActionEvent actionEvent)
    {
        switch (actionEvent.getActionCommand())
        {
            /**
             * Send KST button clicked: Attempt to send the amount to the given
             * address.
             * 
             * However, inform the user if the given address or amount is
             * invalid.
             * 
             * Also, prompt the user to confirm their transaction to prevent
             * accidents.
             */
            case "transaction.send":
                // Make sure that the amount provided is positive and below or
                // eqaul to their total balance.
                try
                {
                    int amount = Integer.parseInt(amountField.getText());
                    String password = JOptionPane.showInputDialog(this, "Password: ", "Enter Krist Password", JOptionPane.PLAIN_MESSAGE);
                    
                    if (amount >= 0 && promptUser("Confirm Transaction", "Recipient Address: " + recipientField.getText() + "\nAmount: " + amount + "\nOf Total: " + Utils.getBalance(Utils.generateAddressV2(password)), true))
                    {
                        if(!password.equals(""))
                        {
                            promptUser(
                                "Transaction Output",
                                Utils.makeTransaction(password, recipientField.getText(), amount) == true ? "Success" : "Failure"
                            );
                        }
                    }
                }
                catch (NumberFormatException conversionFaliure)
                {
                    promptUser("Transaction Failure", "Invalid amount.");
                }
                
                break;
                
            /**
             * Cancel Transaction button clicked: Close the transaction window.
             */
            case "transaction.cancel":
                windowClosing(null);
                break;
        }
    }
    
    /**
     * Wrapper to <code>promptUser(String title, String message, boolean isYesNoPrompt)</code>. Always
     * produces a prompt with an "OK" option.
     * 
     * @param title Title of the prompt window.
     * @param message Message to display within the prompt window.
     */
    private void promptUser(String title, String message)
    {
        promptUser(title, message, false);
    }
    
    /**
     * Prompts the user with a dialog box using the given title, message, and
     * type of prompt. If it is a "yes or no" prompt, then the user will be
     * given an option prompt. Otherwise, the prompt will read "OK" as the only
     * option.
     * 
     * @param title Title of the prompt window.
     * @param message Message to display within the prompt window.
     * @param isYesNoPrompt Whether or not the prompt window will give the user a 'yes' or 'no' choice.
     * @return Whether or not the user clicked 'yes' on the yes no prompt. False if not or was an 'ok' prompt.
     */
    private boolean promptUser(String title, String message, boolean isYesNoPrompt)
    {
        if(isYesNoPrompt)
            return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION) == 0;
        
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
        return false;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // NECESSARY OVERRIDES                                                    //
    ////////////////////////////////////////////////////////////////////////////
    
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
