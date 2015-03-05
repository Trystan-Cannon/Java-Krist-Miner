package krist.miner;

import gui.ManagerGUI;
import java.util.ArrayList;

public class Foreman implements Runnable
{
    private final ManagerGUI gui;
    private final ArrayList<ClusterMiner> miners;
    
    public Foreman (ManagerGUI gui, ArrayList<ClusterMiner> miners)
    {
        this.gui    = gui;
        this.miners = miners;
    }
    
    @Override
    public void run()
    {
        while (gui.isMining())
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
            
            int speed = 0;
            for (ClusterMiner miner : miners)
            {
                speed += miner.getSpeed();
            }
            
            gui.speedTextField.setText ("Hashes/s: " + speed);
        }
        
        gui.speedTextField.setText ("Hashes/s: 0");
    }
}
