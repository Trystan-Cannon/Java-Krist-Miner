package krist.miner;

import gui.ManagerGUI;

public class ClusterMiner implements Runnable
{
    private ManagerGUI gui;
    private String     minerID;
    private int        nonce;
    private String     block;
    private boolean    isComplete;
    private boolean    solvedBlock;
    
    public ClusterMiner (ManagerGUI gui, String minerID, String block, int nonce)
    {
        this.gui     = gui;
        this.minerID = minerID;
        this.nonce   = nonce;
        this.block   = block;
    }
    
    @Override
    public void run()
    {
        String newBlock = Utils.subSHA256 (minerID + block + nonce, 12);
        gui.addOutputLine ("Starting at " + block + " from " + nonce + "!");
        
        for (int hashIteration = 0; hashIteration < ManagerGUI.nonceOffset && newBlock.compareTo (block) >= 0; hashIteration++, nonce++)
        {
            /**
             * This is shit design. @see<code>ManagerGUI.onMineCompletion</code>.
             * 
             * If this doesn't happen, then when the mining is force stopped, the
             * miner will trigger its completion, making the ManagerGUI think that
             * we just need to increase the range, leading the program to continue
             * mining.
             */
            if (!gui.isMining())
            {
                return;
            }
            
            newBlock = Utils.subSHA256 (minerID + block + nonce, 12);
        }
        
        if (newBlock.compareTo (block) < 0)
        {
            gui.addOutputLine ("Solution found at nonce = " + (nonce - 1));
            Utils.submitSolution (minerID, nonce - 1);
            solvedBlock = true;
            
            gui.stopMining();
        }
        
        gui.onMineCompletion (this);
        isComplete = true;
    }
    
    public boolean isComplete()
    {
        return isComplete;
    }
    
    public boolean solvedBlock()
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
}
