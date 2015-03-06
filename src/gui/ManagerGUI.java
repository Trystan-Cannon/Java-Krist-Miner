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
import krist.miner.Foreman;
import krist.miner.MiningListener;
import krist.miner.Utils;

public final class ManagerGUI extends JFrame implements ActionListener, MiningListener
{
    public static final int DEFAULT_MAX_CORE_LIMIT = 1;
    public static final int MAX_CORE_LIMIT         = 8;
    
    private static int configuredCoreLimit = DEFAULT_MAX_CORE_LIMIT; /** The core limit read from the configuration file. */
    
    public static final int WINDOW_WIDTH  = 300;
    public static final int WINDOW_HEIGHT = 480;
    
    public static long nonceOffset = 10000000;
    
    private ArrayList<ClusterMiner> miners = null;
    private volatile boolean isMining      = false;
    private int     finishedMiners         = 0;
    
    public JLabel minerID_fieldLabel    = null;
    public JTextField minerID_textField = null;
    
    public JTextField balanceTextField = null;
    public JTextField speedTextField   = null; /** This is for only the first thread! It is multiplied by the number of threads to estimate the actual speed. */
    
    public JTextArea   outputTextArea   = null;
    public JScrollPane outputScrollPane = null;
    
    public JButton beginMiningButton = null;
    public JButton stopMiningButton  = null;
    
    public ArrayList<JCheckBox> coreUseCheckBoxes = null;
    
    public ManagerGUI()
    {
        // Set up our window's basic characteristics.
        super ("Grim's Krist Miner");
        setSize (WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable (false);
        setDefaultCloseOperation (EXIT_ON_CLOSE);
        
        setLayout (new FlowLayout (FlowLayout.LEADING, 30, 10));
        
        /**
         * Read the configuration file for the configured core limit, if there
         * is one.
         */
        configuredCoreLimit = Utils.getConfiguredCoreLimit();
        
        minerID_fieldLabel = new JLabel ("Krist Address");
        minerID_textField  = new JTextField (21);
        
        balanceTextField = new JTextField (21);
        speedTextField   = new JTextField (21);
        
        outputTextArea   = new JTextArea (10, 20);
        outputScrollPane = new JScrollPane (outputTextArea);

        coreUseCheckBoxes = new ArrayList();
        
        // This creates 6 core options. This is for the sake of the display
        // being uninterrupted; some machines will have more or less cores.
        for (int core = 0; core < 8; core++)
        {
            coreUseCheckBoxes.add (new JCheckBox ("Core " + (core + 1)));
            
            // Prevent the user from using all of their cores.
            if (core >= configuredCoreLimit)
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
        
        balanceTextField.setEditable (false);
        speedTextField.setEditable (false);
        speedTextField.setText ("Hashes/s: 0");
        
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
        add (balanceTextField);
        add (speedTextField);
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
    
    /**
     * Causes the miner to wait until all miner threads are ready to being
     * mining.
     * @param you 
     */
    public synchronized void signifyMinerReady (ClusterMiner you)
    {
        try
        {
            this.wait();
        }
        catch (InterruptedException waitFaliure)
        {
            System.out.println ("ManagerGUI failed to wait for miner start. Stopping mining.");
            stopMining();
        }
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
            if (miner.hasSolvedBlock())
            {
                // Move on to the next cluster if we've solved the problem.
                // Clear the text area, too.
                stopMining();
                outputTextArea.setText ("");
                addOutputLine ("Solved block " + miner.getBlock());
                
                updateBalanceField();
                startMining();
                return;
            }
        }
        
        // If no one has solved it yet, let's try again at a different starting point.
        // Also, make sure that we're still on the same block.
        if (miners.size() == finishedMiners && miners.size() > 0)
        {
            isMining = false;
            
            if (miners.get (0).getCurrentBlock().equals (Utils.getLastBlock()))
            {
                addOutputLine ("\nCould not find solution. Starting at new offset.");
                startMining (miners.get (miners.size() - 1).getNonce());
            }
            
            else
            {
                outputTextArea.setText ("");
                addOutputLine ("Block changed. Moving on to next block.");
                startMining();
            }
        }
    }
    
    public void updateBalanceField()
    {
        balanceTextField.setText ("Retrieving balance...");
        
        String balance = null;
        while (balance == null)
        {
            balance = Utils.getBalance (minerID_textField.getText());
        }
        
        balanceTextField.setText ("Balance: " + balance + " KST");
    }
    
    public void addOutputLine (String line)
    {
        outputTextArea.setText (outputTextArea.getText() + line + "\n");
    }
    
    public String getMinerID()
    {
        return minerID_textField.getText().length() == 0 ? "N\\A" : minerID_textField.getText();
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
            // Update the balance field.
            updateBalanceField();
            
            // Destroy the old miner threads.
            miners = new ArrayList();

            isMining       = true;
            finishedMiners = 0;
            
            String block = Utils.getLastBlock();
            
            for (int miner = 0; miner < configuredCoreLimit; miner++)
            {
                if (coreUseCheckBoxes.get (miner).isSelected())
                {
                    miners.add (new ClusterMiner (this, minerID_textField.getText(), block, startingNonce + nonceOffset * miner));
                    new Thread (miners.get (miner)).start();
                }
            }
            
            new Thread (new Foreman (this, miners)).start();
            
            try
            {
                Thread.sleep (100);
            }
            catch (InterruptedException sleepFailure)
            {
                System.out.println ("Manager failed to sleep. Stopping.");
                stopMining();
            }
            
            synchronized (this)
            {
                this.notifyAll();
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
    
    public static void setCoreLimit (int coreLimit)
    {
        if (coreLimit >= DEFAULT_MAX_CORE_LIMIT && coreLimit <= MAX_CORE_LIMIT)
        {
            configuredCoreLimit = coreLimit;
        }
    }
}
