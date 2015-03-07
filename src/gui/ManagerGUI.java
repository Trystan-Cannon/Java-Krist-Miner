package gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import krist.miner.ClusterMiner;
import krist.miner.Foreman;
import krist.miner.MiningListener;
import krist.miner.Utils;

public final class ManagerGUI extends JFrame implements ActionListener, MiningListener
{
    public static final int DEFAULT_MAX_CORE_LIMIT = 1;
    public static final int MAX_CORE_LIMIT         = 8;
    
    private static int configuredCoreLimit = DEFAULT_MAX_CORE_LIMIT; /** The core limit read from the configuration file. */
    
    private static final int FIELD_WIDTH             = 21;
    private static final int CORE_OUTPUT_FIELD_WIDTH = 11;
    
    public static final int WINDOW_WIDTH  = 300;
    public static final int WINDOW_HEIGHT = 360;
    
    public static final long nonceOffset = 10000000;
    
    private ArrayList<ClusterMiner> miners = null;
    private Foreman foreman                = null;
    private boolean isMining               = false;
    private int     finishedMiners         = 0;
    
    public JLabel minerID_fieldLabel    = null;
    public JTextField minerID_textField = null;
    
    public JTextField blockTextField   = null;
    public JTextField balanceTextField = null;
    public JTextField speedTextField   = null; /** This is for only the first thread! It is multiplied by the number of threads to estimate the actual speed. */
    
    private JTextField blocksMinedField = null;
    private int        blocksMined      = 0;
    
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
        
        setLayout (new FlowLayout (FlowLayout.LEADING, 30, 3));
        
        /**
         * Read the configuration file for the configured core limit, if there
         * is one.
         */
        configuredCoreLimit = Utils.getConfiguredCoreLimit();
        
        /**
         * Initialize all of the panel components that we'll be using.
         */
        minerID_fieldLabel = new JLabel ("Krist Address");
        minerID_textField  = new JTextField (FIELD_WIDTH);
        
        balanceTextField = new JTextField (FIELD_WIDTH);
        speedTextField   = new JTextField (FIELD_WIDTH);
        blockTextField   = new JTextField (FIELD_WIDTH);
        blocksMinedField = new JTextField (FIELD_WIDTH);
        
        coreUseCheckBoxes = new ArrayList();
        
        // This creates 6 core options. This is for the sake of the display
        // being uninterrupted; some machines will have more or less cores.
        for (int core = 0; core < MAX_CORE_LIMIT; core++)
        {
            JCheckBox coreCheckBox = new JCheckBox ("Core " + (core + 1));
            
            coreUseCheckBoxes.add (coreCheckBox);
            
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
        balanceTextField.setBorder (BorderFactory.createTitledBorder ("Balance"));
        speedTextField.setEditable (false);
        speedTextField.setBorder (BorderFactory.createTitledBorder ("Speed (Hashes/s)"));
        speedTextField.setText ("0");
        blockTextField.setEditable (false);
        blockTextField.setBorder (BorderFactory.createTitledBorder ("Block"));
        blocksMinedField.setEditable (false);
        blocksMinedField.setBorder (BorderFactory.createTitledBorder ("Blocks Mined"));
        blocksMinedField.setText ("0");
        
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
        add (blockTextField);
        add (blocksMinedField);
        add (beginMiningButton);
        add (stopMiningButton);
        
        // Add the use check boxes for each core.
        for (int minerIndex = 0; minerIndex < MAX_CORE_LIMIT; minerIndex++)
        {
            add (coreUseCheckBoxes.get (minerIndex));
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
                minerID_textField.setText ("Invalid ID or timeout.");
            }
            // Begin mining. Make sure we're not already mining, though.
            else if (!isMining)
            {
                startMining();
            }
        }
        else if (componentName.equals ("mining.stop"))
        {
            stopMining();
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
                stopMining();
                blocksMined++;
                
                updateBlocksMinedField();
                updateBalanceField();
                startMining();
                return;
            }
        }
        
        // If no one has solved it yet, let's try again at a different starting point.
        // Also, make sure that we're still on the same block.
        if (miners.size() == finishedMiners && miners.size() > 0)
        {
            stopMining();
            
            if (miners.get (0).getCurrentBlock().equals (Utils.getLastBlock()))
            {
                startMining (miners.get (miners.size() - 1).getNonce());
            }
            else
            {
                startMining();
            }
        }
    }
    
    public synchronized void updateSpeedField (long speed)
    {
        speedTextField.setText ("" + speed);
    }
    
    public void updateBlocksMinedField()
    {
        blocksMinedField.setText ("" + blocksMined);
    }
    
    public void updateBalanceField()
    {
        balanceTextField.setText ("Retrieving balance...");
        
        String balance = null;
        while (balance == null)
        {
            balance = Utils.getBalance (minerID_textField.getText());
        }
        
        balanceTextField.setText (balance + " KST");
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
    
    public void startMining (long startingNonce)
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
            blockTextField.setText (block);
            
            for (int miner = 0; miner < configuredCoreLimit; miner++)
            {
                if (coreUseCheckBoxes.get (miner).isSelected())
                {
                    miners.add (new ClusterMiner (this, minerID_textField.getText(), block, startingNonce + nonceOffset * miner));
                    new Thread (miners.get (miner)).start();
                }
            }
            
            foreman = new Foreman (this, miners);
            new Thread (foreman).start();
            
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
            blockTextField.setText ("");
            isMining = false;
            
            // Stop the foreman explicitly.
            foreman.stopMining();
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