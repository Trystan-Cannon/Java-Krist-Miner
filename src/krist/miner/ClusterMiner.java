package krist.miner;

import gui.ManagerGUI;
import javax.swing.JTextField;

public class ClusterMiner implements Runnable
{
    private final String     block;
    private final ManagerGUI gui;
    private final JTextField outputField;
    private final String     minerID;
    
    private long    nonce;
    private int     speed;
    private boolean isComplete;
    private boolean solvedBlock;
    
    public ClusterMiner(ManagerGUI gui, JTextField outputField, String minerID, String block, long nonce)
    {
        this.gui = gui;
        this.outputField = outputField;
        
        this.minerID = minerID;
        this.nonce = nonce;
        this.speed = 0;
        this.block = block;
    }
    
    @Override
    public void run()
    {
        // Informat the manager that we're ready to begin mining.
        gui.signifyMinerReady(this);
        
        String newBlock = Utils.subSHA256(minerID + block + nonce, 12);
        outputField.setText("@" + nonce);
        
        long startTime = System.nanoTime();
        int lastHash = 0;
        
        for (int hashIteration = 0; hashIteration < ManagerGUI.nonceOffset && newBlock.compareTo(block) >= 0; hashIteration++, nonce++)
        {
            /**
             * This is shit design.
             * 
             * @see<code>ManagerGUI.onMineCompletion</code>.
             *
             * If this doesn't happen, then when the mining is force stopped,
             * the miner will trigger its completion, making the ManagerGUI
             * think that we just need to increase the range, leading the
             * program to continue mining.
             */
            if (!gui.isMining())
            {
                outputField.setText("Not in use.");
                return;
            }
            
            // Calculate speed.
            if (System.nanoTime() - startTime > 1E9)
            {
                speed = hashIteration - lastHash;
                lastHash = hashIteration;
                startTime = System.nanoTime();
            }
            
            newBlock = Utils.subSHA256(minerID + block + nonce, 12);
        }
        
        if (newBlock.compareTo(block) < 0)
        {
            outputField.setText("Sln @ " + (nonce - 1));
            Utils.submitSolution(minerID, nonce - 1);
            solvedBlock = true;
            
            gui.stopMining();
        }
        
        gui.onMineCompletion(this);
        outputField.setText("Not in use.");
        isComplete = true;
    }
    
    public String getBlock()
    {
        return block;
    }
    
    public boolean isComplete()
    {
        return isComplete;
    }
    
    public boolean hasSolvedBlock()
    {
        return solvedBlock;
    }
    
    public String getCurrentBlock()
    {
        return block;
    }
    
    public long getNonce()
    {
        return nonce;
    }
    
    public int getSpeed()
    {
        return speed;
    }
}
