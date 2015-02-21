package krist.miner;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import gui.ManagerGUI;

public final class Miner implements Runnable
{
    
    private String userAddress        = null;
    private String submitSolutionLink = null;
    private String balanceLink        = null;
    
    ManagerGUI gui = null; // The gui object from which this miner was instantiated.
    
    public Miner (ManagerGUI gui, String minerID, int nonce)
    {
        this.gui         = gui;
        this.userAddress = minerID;
    }
    
    @Override
    public void run()
    {
        //context.updateBalance (getBalance());
        mineBlocks();
    }
    
    /*
        Mining strategy:
            - Get last block.
            - Increment nonce.
            - Concatenate our address, the last block, and nonce; then hash it.
            - Check if the hash is less than the last block.
                - If so, submit it. Bank some Krist.
                - If not, then try again. And again. And again...
    */
    public void mineBlocks ()
    {
        int nonce = 0;
        
        // This is some shitty design right here.
        gui.addOutputLine ("Retrieving block...");
        String lastBlock = Utils.getLastBlock();
        String newBlock  = null;
        gui.addOutputLine ("Block retrieved.");
        
        while (gui.isMining())
        {
            gui.addOutputLine ("Starting " + lastBlock + " from nonce = " + nonce + ".");
            
            do
            {
                newBlock = Utils.subSHA256 (userAddress + lastBlock + nonce, 12);
                
                for (int hashIteration = 0; hashIteration < 10000000 && newBlock.compareTo (lastBlock) >= 0 && gui.isMining(); hashIteration++, nonce++)
                {
                    gui.updateNonce (nonce);
                    newBlock = Utils.subSHA256 (userAddress + lastBlock + nonce, 12);
                    
                    // We'll add a dampener here. This should limit the CPU intensity.
                    try
                    {
                        Thread.sleep (gui.getSleepTime());
                        //Thread.sleep (context.getSleepTime());
                    }
                    catch (Exception sleepException)
                    {
                        gui.addOutputLine ("Sleep exception. Aborting.");
                        gui.stopMining();
                    }
                }
                
                
                String currentBlock = Utils.getLastBlock();
                
                // Make sure someone didn't already crack this one.
                if (!lastBlock.equals (currentBlock))
                {
                    gui.addOutputLine ("Last block changed.\nMoving on to " + currentBlock + " from nonce = 0.");
                    lastBlock = currentBlock;
                    nonce     = 0;
                }
            } while (newBlock.compareTo (lastBlock) >= 0 && gui.isMining());
            
            // Found a valid solution.
            if (gui.isMining() && newBlock.compareTo (lastBlock) < 0)
            {
                gui.addOutputLine ("Solution found at nonce = " + (nonce - 1));
                gui.addOutputLine ("Current balance " + Utils.getBalance (userAddress) + "\n");
                Utils.submitSolution (userAddress, nonce - 1);
            }
            
            nonce = 0;
            
            gui.addOutputLine ("Fetching latest block...");
            lastBlock = Utils.getLastBlock();
            gui.addOutputLine ("Block retrieved.");
        }
        
        gui.addOutputLine ("Miner halted successfully.");
    }
}
