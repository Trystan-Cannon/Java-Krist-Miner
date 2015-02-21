package gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;
import krist.miner.Miner;
import krist.miner.Utils;

public class ManagerGUI extends JFrame implements ActionListener
{
    public static final int WINDOW_WIDTH  = 300;
    public static final int WINDOW_HEIGHT = 400;
    
    private boolean isMining = false;
    
    public JLabel minerID_fieldLabel    = null;
    public JTextField minerID_textField = null;
    
    public JTextField nonceTextField = null;
    
    public JLabel miningSleepTimerLabel         = null;
    public JTextField miningSleepTimerTextField = null;
    
    public JTextArea   outputTextArea   = null;
    public JScrollPane outputScrollPane = null;
    
    public JButton beginMiningButton = null;
    public JButton stopMiningButton  = null;
    
    private ManagerGUI()
    {
        // Set up our window's basic characteristics.
        super ("Grim's Krist Miner");
        setSize (WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable (false);
        setDefaultCloseOperation (EXIT_ON_CLOSE);
        
        setLayout (new FlowLayout (FlowLayout.LEADING, 30, 10));
        
        minerID_fieldLabel = new JLabel ("Miner ID");
        minerID_textField  = new JTextField (21);
        
        nonceTextField = new JTextField (21);
        
        miningSleepTimerLabel     = new JLabel ("Sleep time (ms)");
        miningSleepTimerTextField = new JTextField (21);
        
        outputTextArea   = new JTextArea (10, 20);
        outputScrollPane = new JScrollPane (outputTextArea);
        
        nonceTextField.setEditable (false);
        
        outputTextArea.setEditable (false);
        // Make sure that the scroll pane moves as the output moves out of view. This looks like automatic scrolling.
        ((DefaultCaret) outputTextArea.getCaret()).setUpdatePolicy (DefaultCaret.ALWAYS_UPDATE);
        outputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        outputScrollPane.setAlignmentY (RIGHT_ALIGNMENT);
        
        beginMiningButton = new JButton ("Begin Mining");
        stopMiningButton  = new JButton ("Stop Mining");
        
        beginMiningButton.setActionCommand ("mining.start");
        stopMiningButton.setActionCommand ("mining.stop");
        beginMiningButton.addActionListener (this);
        stopMiningButton.addActionListener (this);
        
        add (minerID_fieldLabel);
        add (minerID_textField);
        add (miningSleepTimerLabel);
        add (miningSleepTimerTextField);
        add (nonceTextField);
        add (beginMiningButton);
        add (stopMiningButton);
        add (outputScrollPane);
    }
    
    public static void main (String[] args)
    {
        new ManagerGUI().setVisible (true);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        String componentName = actionEvent.getActionCommand();
        
        if (componentName.equals ("mining.start"))
        {
            // Not a valid miner ID: The field is empty.
            if (!Utils.isMinerValid (minerID_textField.getText()))
            {
                minerID_textField.setText ("Invalid ID.");
            }
            // Begin mining. Make sure we're not already mining, though.
            else if (!isMining)
            {
                addOutputLine ("Mining started.");
                new Thread (new Miner (this, minerID_textField.getText(), 0)).start();
                
                isMining = true;
            }
        }
        else if (componentName.equals ("mining.stop"))
        {
            stopMining();
        }
    }
    
    public void addOutputLine (String line)
    {
        outputTextArea.setText (outputTextArea.getText() + line + "\n");
    }
    
    public void updateNonce (int nonce)
    {
        nonceTextField.setText ("Nonce = " + nonce);
    }
    
    public String getMinerID()
    {
        return minerID_textField.getText().length() == 0 ? "N\\A" : minerID_textField.getText();
    }
    
    public int getSleepTime()
    {
        String sleepTime = miningSleepTimerTextField.getText();
        
        try
        {
            int sleepTimer = Integer.parseInt (sleepTime);
            return sleepTimer;
        }
        catch (NumberFormatException exception)
        {
            miningSleepTimerTextField.setText ("Invalid time.");
        }
        
        return 0;
    }
    
    public boolean isMining()
    {
        return isMining;
    }
    
    public void stopMining()
    {
        addOutputLine ("Mining stopped.");
        isMining = false;
    }
}