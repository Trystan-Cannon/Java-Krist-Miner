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
                speed += (miner.getNonce() - miner.getStartNonce()) / secondsElapsed;
            }
            
            gui.updateSpeedField (speed);
        }
        
        gui.updateSpeedField (0);
    }
    
    public synchronized void stopMining()
    {
        System.out.println ("Foreman " + this + " stopped.");
        isMining = false;
    }
}
