package krist.miner;

import gui.ManagerGUI;

public class ClusterMiner implements Runnable
{
    private final String     block;
    private final ManagerGUI gui;
    private final String     minerID;
    private final long       startNonce;
    
    private long    nonce;
    private boolean isComplete;
    private boolean solvedBlock;
    
    public ClusterMiner (ManagerGUI gui, String minerID, String block, long nonce)
    {
        this.gui        = gui;
        this.minerID    = minerID;
        this.startNonce = nonce;
        this.nonce      = nonce;
        this.block      = block;
    }
    
    @Override
    public void run()
    {
        // Informat the manager that we're ready to begin mining.
        gui.signifyMinerReady (this);
        String newBlock = Utils.subSHA256(minerID + block + nonce, 12);
        
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
                return;
            }
            newBlock = Utils.subSHA256(minerID + block + nonce, 12);
        }
        
        if (newBlock.compareTo(block) < 0)
        {
            Utils.submitSolution(minerID, nonce - 1);
            solvedBlock = true;
            
            System.out.println("Got solution "+newBlock+ " for "+block);
            
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
    
    public synchronized long getNonce()
    {
        return nonce;
    }
    
    public synchronized long getStartNonce()
    {
        return startNonce;
    }
}
