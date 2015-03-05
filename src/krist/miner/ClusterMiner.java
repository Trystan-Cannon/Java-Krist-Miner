package krist.miner;

import gui.InitializationGUI;
import gui.ManagerGUI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClusterMiner implements Runnable
{
    private final String     block;
    private final ManagerGUI gui;
    private final String     minerID;
    
    private int     nonce;
    private int     speed;
    private boolean isComplete;
    private boolean solvedBlock;
    
    public ClusterMiner (ManagerGUI gui, String minerID, String block, int nonce)
    {
        this.gui     = gui;
        this.minerID = minerID;
        this.nonce   = nonce;
        this.speed   = 0;
        this.block   = block;
    }
    
    @Override
    public void run()
    {
        // Informat the manager that we're ready to begin mining.
        gui.signifyMinerReady (this);
        
        String newBlock = Utils.subSHA256(minerID + block + nonce, 12);
        gui.addOutputLine("Starting at " + block + " from " + nonce + "!");

        long startTime = System.nanoTime();
        int lastHash   = 0;

        for (int hashIteration = 0; hashIteration < ManagerGUI.nonceOffset && newBlock.compareTo(block) >= 0; hashIteration++, nonce++)
        {

            /**
             * This is shit design.
             * @see<code>ManagerGUI.onMineCompletion</code>.
             *
             * If this doesn't happen, then when the mining is force stopped,
             * the miner will trigger its completion, making the ManagerGUI
             * think that we just need to increase the range, leading the
             * program to continue mining.
             */
            if (!gui.isMining())
            {
                return;
            }

            // Calculate speed.
            if (System.nanoTime() - startTime > 1E9)
            {
                speed     = hashIteration - lastHash;
                lastHash  = hashIteration;
                startTime = System.nanoTime();
            }

            newBlock = Utils.subSHA256(minerID + block + nonce, 12);
        }

        if (newBlock.compareTo(block) < 0)
        {
            gui.addOutputLine("Solution found at nonce = " + (nonce - 1));
            Utils.submitSolution(minerID, nonce - 1);
            solvedBlock = true;

            gui.stopMining();
        }

        gui.onMineCompletion(this);
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
    
    public int getNonce()
    {
        return nonce;
    }
    
    public int getSpeed()
    {
        return speed;
    }
}
