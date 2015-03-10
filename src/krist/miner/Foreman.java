package krist.miner;

import gui.ManagerGUI;
import java.util.ArrayList;

public class Foreman implements Runnable
{
    private final ManagerGUI gui;
    private final ArrayList<ClusterMiner> miners;
    
    private int secondsElapsed;
    private volatile boolean isMining;
    
    public Foreman (ManagerGUI gui, ArrayList<ClusterMiner> miners)
    {
        this.gui    = gui;
        this.miners = miners;
        
        this.secondsElapsed = 0;
        this.isMining       = true;
    }
    
    @Override
    /**
     * Computes the hash rate of all of the miners contained within
     * <code>this.miners</code>, updating the ManagerGUI's hash rate
     * field.
     * 
     * The calculation is performed approximately every 1 second.
     */
    public void run()
    {
        while (isMining)
        {
            try
            {
                synchronized (this)
                {
                    wait (1000);
                }
            }
            catch (InterruptedException sleepFailure)
            {
                System.out.println ("Foreman failed to sleep.");
            }
            secondsElapsed++;
            
            long speed = 0;
            for (ClusterMiner miner : miners)
            {
                speed += miner.getChangeInNonce() / secondsElapsed;
            }
            
            gui.updateSpeedField (speed);
        }
        
        gui.updateSpeedField (0);
    }
    
    /**
     * Stops the foreman from working, killing its thread.
     * 
     * This is necessary because the foreman will synchronize on itself
     * in its <code>run</code> method, so it may not be checking if the
     * ManagerGUI has stopped before starting to mine at new offsets.
     */
    public synchronized void stopMining()
    {
        isMining = false;
    }
}
