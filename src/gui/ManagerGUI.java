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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import krist.miner.ClusterMiner;
import krist.miner.Foreman;
import krist.miner.MiningListener;
import krist.miner.Utils;
import krist.wallet.*;

public final class ManagerGUI extends JFrame implements ActionListener, MiningListener
{
    public static final int DEFAULT_MAX_CORE_LIMIT = 1;
    public static final int MAX_CORE_LIMIT         = 8;
    
    private static int configuredCoreLimit = DEFAULT_MAX_CORE_LIMIT; /** The core limit read from the configuration file. */
    
    private static final int FIELD_WIDTH = 21;
    
    public static final int WINDOW_WIDTH  = 300;
    public static final int WINDOW_HEIGHT = 380;
    
    public static final long nonceOffset = 10000000;
    
    private ArrayList<ClusterMiner> miners = null;
    private Foreman foreman                = null;
    private boolean isMining               = false;
    private int     finishedMiners         = 0;
    
    /**
     * The current block that the miner is working on. This may or may not
     * be the latest block, however.
     */
    private String currentBlock;
    
    /**
     * The menu bar which appears atop the window.
     * From this menu bar, users will be able to open other useful windows,
     * namely the wallet window.
     */
    private final JMenuBar topMenuBar;
    
    /**
     * Contains all wallet options that the user may open from
     * the main interface. These include but are not limited to:
     *  - The transaction window
     *  - The block chain window
     *  - The top addresses window
     */
    private final JMenu walletMenu;
    
    /**
     * The wallet related windows available from the wallet menu.
     * When these items are clicked, the corresponding window will be spawned
     * alongside the main interface.
     */
    private final JMenuItem transactionWindow, blockChainWindow, topAddressesWindow;
    
    /**
     * The instance of the <code>TransactionGUI</code> that is the transaction
     * window. There may be only one at a time.
     */
    private TransactionGUI transactionInterface;
    
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
        
        // Create the menu bar and its corresponding menus, respectively.
        topMenuBar = new JMenuBar();
        walletMenu = new JMenu ("Wallet");
        
        // Create all of the wallet menu items that will appear under the
        // wallet menu in the top menu bar.
        transactionWindow  = new JMenuItem ("Make a transaction");
        blockChainWindow   = new JMenuItem ("Block chain");
        topAddressesWindow = new JMenuItem ("Top Addresses");
        
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
        
        // Add all of the menus and menu items for the top menu bar.
        topMenuBar.add (walletMenu);
        
        // Setup action listeners for all of the wallet menu items. This way,
        // we can spawn the proper window when the corresponding menu item is clicked.
        // Each item should have a specified action command, so it is recognizable in actionPerformed.
        transactionWindow.setActionCommand ("wallet.menu.transactionWindow");
        transactionWindow.addActionListener (this);
        blockChainWindow.setActionCommand ("wallet.menu.blockChainWindow");
        blockChainWindow.addActionListener (this);
        topAddressesWindow.setActionCommand ("wallet.menu.topAddressesWindow");
        topAddressesWindow.addActionListener (this);
        
        // Add the wallet menu items to the wallet menu, so they appear underneath
        // it when clicked.
        walletMenu.add (transactionWindow);
        walletMenu.add (blockChainWindow);
        walletMenu.add (topAddressesWindow);
        
        setJMenuBar (topMenuBar);
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
    
    /**
     * Closes the given window by disposing of it and removing the reference
     * from our instance.
     * 
     * @param actionCommand The same name which appears when opened from a menu i.e.
     *                      whatever was passed to setActionCommand.
     */
    public void closeWindow (String actionCommand)
    {
        // Transaction window closed:
        if (actionCommand.equals ("wallet.menu.transactionWindow"))
        {
            transactionInterface.dispose();
            transactionInterface = null;
        }
    }

    /**
     * Handles any actions performed. The ManagerGUI is an <code>ActionListener</code>.
     * 
     * This way, the ManagerGUI can respond to button clicks and other changes
     * made by the user in the main interface.
     * 
     * @param actionEvent 
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        String componentName = actionEvent.getActionCommand();
        
        switch (componentName)
        {
            /**
             * "Begin Mining" (<code>beginMiningButton</code>) button clicked:
             * Start the mining if we're not already mining.
             */
            case "mining.start":
                // Not a valid miner ID: The field is empty.
                if (!Utils.isMinerValid (minerID_textField.getText()))
                {
                    minerID_textField.setText ("Invalid ID or timeout.");
                }
                // Begin mining. Make sure we're not already mining, though.
                else if (!isMining)
                {
                    startMining (0);
                }

                break;
                
            /**
             * "Stop Mining" (<code>stopMiningButton</code>) button clicked:
             * Stop the miner if we're currently mining. This is checked by
             * <code>stopMining</code>.
             * 
             * @see gui.ManagerGUI.stopMining
             */
            case "mining.stop":
                stopMining();
                break;
                
            /**********************/
            /* Wallet menu items: */
            /**********************/
                
            /**
             * "Transaction" Item:
             * 
             * Transaction window option (<code>JMenuItem transactionWindow</code>) selected
             * from the top menu bar (<code>JMenuBar topMenuBar</code>) in the
             * wallet (<code>JMenu walletMenu</code>) menu.
             */
            case "wallet.menu.transactionWindow":
                // Ensure there is no other transaction window currently open.
                if (transactionInterface == null)
                    transactionInterface = new TransactionGUI (this, this.getX() + WINDOW_WIDTH, this.getY());
                break;
        }
    }
    
    @Override
    /**
     * Executed when a cluster miner finishes its mining.
     */
    public void onMineCompletion(ClusterMiner finishedMiner)
    {
        finishedMiners++;
        
        // Check if any of the miners have solved the block.
        if (finishedMiner.hasSolvedBlock())
        {
            // Move on to the next cluster if we've solved the problem.
            stopMining();
            blocksMined++;
            
            // Update all of our fields, then start mining again at the next
            // target.
            updateBlocksMinedField();
            updateBalanceField();
            startMining (0);
            return;
        }
        
        // If no one has solved it yet, let's try again at a different starting point.
        // Also, make sure that we're still on the same block.
        if (miners.size() == finishedMiners && miners.size() > 0)
        {
            stopMining();
            
            if (currentBlock.equals (Utils.getLastBlock()))
            {
                startMining (miners.get (miners.size() - 1).getNonce());
            }
            else
            {
                startMining (0);
            }
        }
    }
    
    /**
     * Sets the contents of the <code>speedTextField</code> to the given
     * speed.
     * 
     * This method is <code>synchronized</code> because the foreman thread
     * which calls it may be running alongside another foreman due to a bug
     * or late disposal of the previous foreman. This prevents any multi-threading
     * nonsense. However, it may be unnecessary.
     * 
     * @param speed 
     */
    public synchronized void updateSpeedField (long speed)
    {
        speedTextField.setText ("" + speed);
    }
    
    /**
     * Updates the <code>blocksMinedField</code> with the current number
     * of blocks which the miner has successfully solved.
     */
    public void updateBlocksMinedField()
    {
        blocksMinedField.setText ("" + blocksMined);
    }
    
    /**
     * Updates the <code>balanceTextField</code> with the latest balance
     * from the krist server for the address retrieved from
     * <code>getKristAddress</code>, the contents of the <code>minerID_textField</code>.
     */
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
    
    /**
     * Retrieves the contents of the <code>minerID_textField</code> field. This
     * should be the address for which the user wants to mine KST.
     * 
     * @return Contents of the <code>minerID_textField</code>.
     */
    public String getKristAddress()
    {
        return minerID_textField.getText().length() == 0 ? "\\0" : minerID_textField.getText();
    }
    
    /**
     * @return Whether or not the miner is currently mining.
     */
    public boolean isMining()
    {
        return isMining;
    }
    
    /**
     * Starts the manager mining from the given nonce offset. For every cluster
     * miner, the one in front starts @see <code>gui.ManagerGUI.NONCE_OFFSET</code>
     * nonces further ahead. This way, the miners don't do the same work as the
     * others.
     * 
     * @param startingNonce The nonce offset at which to start.
     */
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
            
            currentBlock  = Utils.getLastBlock();
            long   target = Utils.getWork();
            blockTextField.setText (currentBlock);
            
            /**
             * Spawn a new set of <code>krist.miner.ClusterMiner</code> threads which will
             * run on the number of cores configured.
             */
            for (int miner = 0; miner < configuredCoreLimit; miner++)
            {
                if (coreUseCheckBoxes.get (miner).isSelected())
                {
                    miners.add (new ClusterMiner (this, minerID_textField.getText(), currentBlock, target, startingNonce + nonceOffset * miner));
                    new Thread (miners.get (miner)).start();
                }
            }
            
            try
            {
                Thread.sleep (100);
            }
            catch (Exception e)
            {
            }
            
            /**
             * Create a new <code>krist.miner.Foreman</code> object to
             * "supervise" the new miners.
             * 
             * Essentially, the foreman will serve to compute the hash rate
             * of the program as a whole, not just each miner individually.
             */
            foreman = new Foreman (this, miners);
            new Thread (foreman).start();
            
            /*
                Inform all of the miners which have called 'signifyMinerReady,'
                allowing them to start mining.
            
                This way, the miners won't begin before the foreman does.
            */
            synchronized (this)
            {
                this.notifyAll();
            }
        }
    }
    
    /**
     * Stops the miner completely. The current foreman is also disposed of.
     *
     * This method only works if the miner is currently mining, otherwise it
     * does nothing.
     */
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
    
    /**
     * Sets the configured core limit for the program. This is (usually) read
     * form the configuration file at @see <code>krist.miner.Utils.CONFIG_FILE_PATH</code>.
     * 
     * @param coreLimit Core limit for the program.
     */
    public static void setCoreLimit (int coreLimit)
    {
        if (coreLimit >= DEFAULT_MAX_CORE_LIMIT && coreLimit <= MAX_CORE_LIMIT)
        {
            configuredCoreLimit = coreLimit;
        }
    }
}