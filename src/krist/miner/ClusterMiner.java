package krist.miner;

import gui.ManagerGUI;
import java.math.BigInteger;

public class ClusterMiner implements Runnable
{
    private final BigInteger target;
    private final String     block;
    private final ManagerGUI gui;
    private final String     minerID;
    private final long       startNonce;
    
    private long    nonce;
    private boolean isComplete;
    private boolean solvedBlock;
    
    public ClusterMiner (ManagerGUI gui, String minerID, String block, long target, long nonce)
    {
        this.gui        = gui;
        this.minerID    = minerID;
        this.startNonce = nonce;
        this.nonce      = nonce;
        this.target     = new BigInteger ("" + target);
        this.block      = block;
    }
    
    @Override
    /**
     * "Mines" the current target as determined by krist's getwork function.
     * @see <code>Utils.getWork</code>.
     * 
     * The mining is performed by using an incremented 'nonce,' a single long
     * integer, and concatenating to the end of the user's krist address and
     * the last block.
     */
    public void run()
    {
        // Inform the manager that we're ready to begin mining.
        gui.signifyMinerReady (this);
        String newBlock;

        for (int hashIteration = 0; hashIteration < ManagerGUI.nonceOffset; hashIteration++, nonce++)
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

            newBlock = Utils.subSHA256(minerID + block + nonce, 12);
            
            /**
             * Calculated a smaller hash? Submit it, take our hard earned KST,
             * and get mining on the next block.
             */
            if (new BigInteger (newBlock, 16).compareTo (target) == -1)
            {
                Utils.submitSolution(minerID, nonce - 1);
                solvedBlock = true;

                gui.stopMining();
                break;
            }
        }

        isComplete = true;
        gui.onMineCompletion(this);
    }
    
    /**
     * Checks whether or not this miner has solved the block that
     * it was given upon instantiation.
     * 
     * @return Whether or not this miner has solved its block.
     */
    public boolean hasSolvedBlock()
    {
        return solvedBlock;
    }
    
    /**
     * Calculates the change in nonces that this miner has made as a way
     * to measure its progress and, consequently, hash rate.
     * 
     * @return The difference between this miner's starting nonce and current nonce.
     */
    public synchronized long getChangeInNonce()
    {
        return nonce - startNonce;
    }
    
    /**
     * @return The current nonce of this miner. Used to compute the next offset
     * at which the miner should start its next batch of <code>ClusterMiner</code> threads.
     */
    public long getNonce()
    {
        return nonce;
    }
}
