package gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;
import krist.miner.ClusterMiner;
import krist.miner.MiningListener;
import krist.miner.Utils;

public class ManagerGUI extends JFrame implements ActionListener, MiningListener
{
    public static final int MAX_CORES = Math.max ((int) (Runtime.getRuntime().availableProcessors() / (3D/2D)), 1);
    
    public static final int WINDOW_WIDTH  = 300;
    public static final int WINDOW_HEIGHT = 460;
    
    public static int nonceOffset = 1000000;
    
    private ArrayList<ClusterMiner> miners = null;
    private boolean isMining               = false;
    private int     finishedMiners         = 0;
    
    public JLabel minerID_fieldLabel    = null;
    public JTextField minerID_textField = null;
    
    public JTextField balanceTextField = null;
    
    public JLabel miningSleepTimerLabel         = null;
    public JTextField miningSleepTimerTextField = null;
    
    public JTextArea   outputTextArea   = null;
    public JScrollPane outputScrollPane = null;
    
    public JButton beginMiningButton = null;
    public JButton stopMiningButton  = null;
    
    public ArrayList<JCheckBox> coreUseCheckBoxes = null;
    
    private ManagerGUI()
    {
        // Set up our window's basic characteristics.
        super ("Grim's Krist Miner");
        setSize (WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable (false);
        setDefaultCloseOperation (EXIT_ON_CLOSE);
        
        setLayout (new FlowLayout (FlowLayout.LEADING, 30, 10));
        
        minerID_fieldLabel = new JLabel ("Krist Address");
        minerID_textField  = new JTextField (21);
        
        balanceTextField = new JTextField (21);
        
        miningSleepTimerLabel     = new JLabel ("Sleep time (ms)");
        miningSleepTimerTextField = new JTextField (21);
        
        outputTextArea   = new JTextArea (10, 20);
        outputScrollPane = new JScrollPane (outputTextArea);

        coreUseCheckBoxes = new ArrayList();
        
        // This creates 6 core options. This is for the sake of the display
        // being uninterrupted; some machines will have more or less cores.
        for (int core = 0; core < 6; core++)
        {
            coreUseCheckBoxes.add (new JCheckBox ("Core " + (core + 1)));
            
            // Prevent the user from using all of their cores.
            if (core >= MAX_CORES)
            {
                coreUseCheckBoxes.get (core).setEnabled (false);
            }
            // Make sure we can see if they're checked.
            else
            {
                coreUseCheckBoxes.get (core).addActionListener (this);
                coreUseCheckBoxes.get (core).setActionCommand ("core.use." + core);
            }
        }
        
        // Set the first core to always be checked.
        coreUseCheckBoxes.get (0).setSelected (true);
        coreUseCheckBoxes.get (0).setEnabled (false);
        
        miningSleepTimerTextField.setText ("0");
        balanceTextField.setEditable (false);
        
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
        add (balanceTextField);
        add (beginMiningButton);
        add (stopMiningButton);
        add (outputScrollPane);
        
        // Add the check boxes for each core we can use.
        for (JCheckBox coreCheckBox : coreUseCheckBoxes)
        {
            add (coreCheckBox);
        }
        
        miners = new ArrayList();
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
                outputTextArea.setText ("If you think this is\na mistake, then there\n"
                                      + "may have been a\nconnection timeout error.\n"
                                      + "If so, please restart\nthe program.");
            }
            // Begin mining. Make sure we're not already mining, though.
            else if (!isMining)
            {
                addOutputLine ("\nMining started.\nRetrieving block...");
                startMining();
            }
        }
        else if (componentName.equals ("mining.stop"))
        {
            stopMining();
            outputTextArea.setText ("");
        }
    }
    
    @Override
    public void onMineCompletion(ClusterMiner finishedMiner)
    {
        finishedMiners++;
        
        // Check if any of the miners have solved the block.
        for (ClusterMiner miner : miners)
        {
            if (miner.solvedBlock())
            {
                // Move on to the next cluster if we've solved the problem.
                // Clear the text area, too.
                stopMining();
                outputTextArea.setText ("");
                
                // Update the balance field.
                balanceTextField.setText ("Balance: " + Utils.getBalance (minerID_textField.getText()) + " KST");
                startMining();
            }
        }
        
        // If no one has solved it yet, let's try again at a different starting point.
        // Also, make sure that we're still on the same block.
        if (miners.size() == finishedMiners && miners.size() > 0)
        {
            isMining = false;
            
            if (miners.get (0).getCurrentBlock().equals (Utils.getLastBlock()))
            {
                addOutputLine ("Could not find solution. Starting at new offset.");
                startMining (miners.get (miners.size() - 1).getNonce());
            }
            
            else
            {
                addOutputLine ("Block changed. Moving on to next block.");
                startMining();
            }
        }
    }
    
    public void addOutputLine (String line)
    {
        outputTextArea.setText (outputTextArea.getText() + line + "\n");
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
    
    public void startMining()
    {
        startMining (0);
    }
    
    public void startMining (int startingNonce)
    {
        if (!isMining)
        {
            // Destroy the old miner threads.
            miners = new ArrayList();

            isMining       = true;
            finishedMiners = 0;
            
            String block = Utils.getLastBlock();
            
            for (int miner = 0; miner < MAX_CORES; miner++)
            {
                if (coreUseCheckBoxes.get (miner).isSelected())
                {
                    miners.add (new ClusterMiner (this, minerID_textField.getText(), block, startingNonce + nonceOffset * miner));
                    new Thread (miners.get (miner)).start();
                }
            }
        }
    }
    
    public void stopMining()
    {
        if (isMining)
        {
            addOutputLine ("Mining stopped.");
            isMining = false;
        }
    }
}