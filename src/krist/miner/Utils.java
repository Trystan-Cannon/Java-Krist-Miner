package krist.miner;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import gui.ManagerGUI;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class Utils
{
    /**
     * Miner configuration file format:
     *  Lines that start with '#' are comments.
     *  Following this, we're looking for the field: coreLimit=...
     * 
     * The default coreLimit is 1.
     */
    private static final String CONFIG_FILE_PATH = "config.txt";
    
    private static final String KRIST_SYNC_LINK   = getPage ("https://raw.githubusercontent.com/BTCTaras/kristwallet/master/staticapi/syncNode").get (0) + "?";
    private static final String LAST_BLOCK_LINK   = KRIST_SYNC_LINK + "lastblock";
    private static final String BALANCE_LINK_BASE = KRIST_SYNC_LINK + "getbalance=";
    
    public static String getLastBlock()
    {
        ArrayList<String> lastBlockPageData = null;
        
        while (lastBlockPageData == null)
        {
            lastBlockPageData = getPage (LAST_BLOCK_LINK);
        }
        
        return lastBlockPageData.get (0);
    }
    
    public static String getBalance (String userAddress)
    {
        ArrayList<String> balanceData = getPage (BALANCE_LINK_BASE + userAddress);
        return balanceData == null ? null : balanceData.get (0);
    }
    
    public static void submitSolution (String minerID, long nonce)
    {
        getPage (KRIST_SYNC_LINK + "submitblock&address=" + minerID + "&nonce=" + nonce);
    }
    
    public static String subSHA256 (String data, int endIndex)
    {
        return Hashing.sha256().hashString (data, Charsets.UTF_8).toString().substring (0, endIndex);
    }
    
    public static boolean isMinerValid (String minerID)
    {
        ArrayList<String> minerValidity = getPage (BALANCE_LINK_BASE + minerID);
        
        // Error retrieving page data.
        return minerValidity != null && !minerValidity.isEmpty();
    }
    
    public static ArrayList<String> getPage (String url)
    {
        try
        {
            URL         lastBlockURL    = new URL (url);
            InputStream pageInputStream = lastBlockURL.openStream();
            BufferedReader pageReader   = new BufferedReader (new InputStreamReader (pageInputStream));
            
            ArrayList<String> lines = new ArrayList();
            String line;
            
            while ((line = pageReader.readLine()) != null)
            {
                lines.add (line);
            }
            
            return lines;
        }
        catch (MalformedURLException malformedException)
        {
            System.out.println (malformedException.getMessage());
        }
        catch (IOException ioException)
        {
            System.out.println (ioException.getMessage());
        }
        
        return null;
    }
    
    public static boolean createConfigurationFile()
    {
        File configurationFile = new File (CONFIG_FILE_PATH);
        
        // Even if we're creating the file, let's be safe and make sure
        // that it doesn't already exist.
        if (!configurationFile.exists())
        {
            try
            {
                configurationFile.createNewFile();

                FileWriter     writerHandle = new FileWriter (configurationFile.getAbsolutePath());
                BufferedWriter writer       = new BufferedWriter (writerHandle);
                
                writer.write ("# This is the maximum allowed number of cores that the miner can utilize.");
                writer.newLine();
                writer.write ("# The default value is 1. The maxmimum is 8. If you choose to use more than");
                writer.newLine();
                writer.write ("# the recommended number of cores, your computer stands the risk of a thermal\n");
                writer.newLine();
                writer.write ("# shutdown or, more  dangerously, damage by overheating. You have been warned.");
                writer.newLine();
                writer.write ("# I, Trystan Cannon, bare no responsibility for your actions.");
                writer.newLine();
                
                writer.write ("coreLimit=" + ManagerGUI.DEFAULT_MAX_CORE_LIMIT);
                writer.close();
                
                return true;
            }
            catch (IOException failureReport)
            {
                System.out.println ("Failed to create new configuration file.");
            }
        }
        
        return false;
    }
    
    public static int getConfiguredCoreLimit()
    {
        File configurationFile = new File (CONFIG_FILE_PATH);
        
        // Make sure that our file exists. If not, then we'll create it.
        if (configurationFile.exists() && !configurationFile.isDirectory())
        {
            try
            {
                FileReader     readerHandle = new FileReader (configurationFile.getAbsolutePath());
                BufferedReader reader       = new BufferedReader (readerHandle);
                
                // Read lines from the file until we hit the core limit.
                String line;
                while ((line = reader.readLine()) != null && !line.startsWith ("coreLimit="))
                {
                }
                
                // Convert the limit to a valid integer.
                try
                {
                    int configuredCoreLimit = Integer.parseInt (line.substring (("coreLimit=").length()));
                    
                    // Make sure the core limit is within the max and min values.
                    if (configuredCoreLimit >= ManagerGUI.DEFAULT_MAX_CORE_LIMIT && configuredCoreLimit <= ManagerGUI.MAX_CORE_LIMIT)
                    {
                        return configuredCoreLimit;
                    }
                }
                catch (Exception conversionFailureReport)
                {
                    System.out.println ("Failed to convert configured core limit to integer.");
                }
            }
            catch (IOException failureReport)
            {
                System.out.println ("Failed to read configuration core limit from existing file.");
            }
        }
        else
        {
            createConfigurationFile();
        }
        
        return ManagerGUI.DEFAULT_MAX_CORE_LIMIT;
    }
}
