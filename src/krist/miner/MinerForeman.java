package krist.miner;

import gui.ManagerGUI;
import java.util.ArrayList;

public class MinerForeman implements Runnable
{
    private ArrayList<ClusterMiner> miners;
    private ManagerGUI     gui;
    
    public MinerForeman (ManagerGUI gui, ArrayList<ClusterMiner> miners)
    {
        this.gui    = gui;
        this.miners = miners;
    }
    
    @Override
    public void run()
    {
        long startTime = System.nanoTime();
        
        while (gui.isMining())
        {
            // 1 second elapsed: recalculate speed.
            if (System.nanoTime() - startTime >= 1E9)
            {
                int speed = 0;
                
                for (ClusterMiner miner : miners)
                {
                    speed += miner.getSpeed();
                }
                
                gui.updateSpeedField (speed);
                startTime = System.nanoTime();
            }
        }
        
        gui.updateSpeedField (0);
    }
}