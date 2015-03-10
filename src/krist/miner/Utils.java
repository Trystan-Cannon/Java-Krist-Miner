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
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


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
    private static final String GET_WORK_LINK     = KRIST_SYNC_LINK + "getwork";
    private static final String BALANCE_LINK_BASE = KRIST_SYNC_LINK + "getbalance=";
    
    /**
     * Retrieves the last block mined from the krist server.
     * 
     * @return Last krist block mined, according to the krist server.
     */
    public static String getLastBlock()
    {
        ArrayList<String> lastBlockPageData = null;
        
        while (lastBlockPageData == null)
        {
            lastBlockPageData = getPage (LAST_BLOCK_LINK);
        }
        
        return lastBlockPageData.get (0);
    }
    
    /**
     * Retrieves the current target block.
     * 
     * @return Current target block from the krist server.
     */
    public static long getWork()
    {
        ArrayList<String> targetData = null;
        
        // Try to get the latest target until we achieve success.
        // This may cause the program to freeze, but it prevents the miner
        // from breaking elsewhere.
        while (targetData == null)
        {
            targetData = getPage (GET_WORK_LINK);
        }
        
        try
        {
            long target = Long.parseLong (targetData.get (0));
            return target;
        }
        catch (NumberFormatException failure)
        {
        }
        
        return -1;
    }
    
    /**
     * Retrieves the balance of the given krist address.
     * 
     * @param userAddress Krist address of which the balance will be retrieved
     * @return User's KST balance as a string
     * (avoid the need to do a try-catch for <code>NumberFormatException</code>).
     */
    public static String getBalance (String userAddress)
    {
        ArrayList<String> balanceData = getPage (BALANCE_LINK_BASE + userAddress);
        return balanceData == null ? null : balanceData.get (0);
    }
    
    /**
     * Submits the miner's solution to the current target, awarding the user
     * KST if their solution is correct and the target has not already changed.
     * 
     * @param minerID Krist address under which the solution will be submitted.
     * @param nonce The nonce at which the miner solved the current target.
     */
    public static void submitSolution (String minerID, long nonce)
    {
        getPage (KRIST_SYNC_LINK + "submitblock&address=" + minerID + "&nonce=" + nonce);
    }
    
    /**
     * Hashes the given data and returns a substring of it starting at 0 and
     * continuing to <code>endIndex</code>.
     * 
     * @param data Data to hash with SHA256.
     * @param endIndex End index at which to stop the substring.
     * @return Substring of the hashed data.
     */
    public static String subSHA256 (String data, int endIndex)
    {
        return SHA256.bytesToHex (SHA256.digest(data.getBytes())).substring (0, endIndex);
        // return Hashing.sha256().hashString (data, Charsets.UTF_8).toString().substring (0, endIndex);
    }
    
    /**
     * Checks whether or not the given krist address is a valid address.
     * 
     * This is achieved by attempting to check the balance of the given
     * address; if it is invalid, then it will have no such page, yielding a
     * null return value from <code>getPage</code>. However, if it is a valid
     * address, then the call to <code>getPage</code> will yield a non-null
     * value.
     * 
     * @param minerID Address of which to check the validity.
     * @return Validity of <code>minerID</code>
     */
    public static boolean isMinerValid (String minerID)
    {
        ArrayList<String> minerValidity = getPage (BALANCE_LINK_BASE + minerID);
        
        // Error retrieving page data.
        return minerValidity != null && !minerValidity.isEmpty();
    }
    
    /**
     * Downloads the source of the page at <code>url</code>. Stored line by line.
     * 
     * @param url The url whose content will be downloaded.
     * @return The page's source, line by line. Null if there was an issue downloading.
     */
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
    
    /**
     * Creates the configuration file from scratch. This file appears in the
     * same directory as the miner jar itself.
     * 
     * Comments in the configuration file are ignored. They begin with '#.'
     * 
     * The configuration file currently contains the following fields:
     *  - coreLimit: The configured core limit for the miner.
     * 
     * @return Creation success or failure.
     */
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
    
    /**
     * Reads the configuration file stored at <code>Utils.CONFIG_FILE_PATH</code>,
     * retrieving the configured core limit (the number of cores/threads for
     * the miner to use).
     * 
     * @return The core limit configured by the user.
     */
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
    
    /**
     * A re-implementation of 'hextobase36' from http://pastebin.com/gSTtpjc7.
     * 
     * @param 'j' from 'hextobase36' in http://pastebin.com/gSTtpjc7.
     * @return The value generated by the original lua function 'hextobase36) from http://pastebin.com/gSTtpjc7.
     */
    private static String hextobase36_lua (BigInteger number)
    {
        for (int i = 6; i <= 251; i += 7)
        {
            if (lessThanOrEqualTo (number, i))
            {
                if (i <= 69)
                {
                    return (char) ('0' + (i - 6) / 7) + "";
                }
                
                return (char) ('a' + ((i - 76) / 7)) + "";
            }
        }
        
        return "e";
    }
    
    /**
     * Checks whether or not the given big integer is less-than-or-equal-to the
     * given integer value.
     * 
     * @param number
     * @param value
     * @return number less-than-or-equal-to value
     */
    private static boolean lessThanOrEqualTo (BigInteger number, int value)
    {
        return number.compareTo (new BigInteger ("" + value)) <= 0;
    }
    
    /**
     * Generates the updated version of a user's krist address given their password.
     * 
     * @param password The user's password.
     *      The password string modified such that:
     *          password = SHA256 ("KRISTWALLET" + password) + "-000"
     * @return Version 2 of a user's krist address.
     */
    public static String generateAddressV2 (String password)
    {
        // Generate the master key for hashing according to http://pastebin.com/gSTtpjc7
        String masterKey = Utils.subSHA256 ("KRISTWALLET" + password, 64) + "-000";
        
        /*
         * Original Lua function:
         * 
         * function makev2address(key)
         *      local protein = {}
         *      local stick = sha256(sha256(key))
         *      local n = 0
         *      local link = 0
         *      local v2 = "k"
         *      repeat
         *        if n < 9 then protein[n] = string.sub(stick,0,2)
         *        stick = sha256(sha256(stick)) end
         *        n = n + 1
         *      until n == 9
         *      n = 0
         *      repeat
         *        link = tonumber(string.sub(stick,1+(2*n),2+(2*n)),16) % 9
         *        if string.len(protein[link]) ~= 0 then
         *          v2 = v2 .. hextobase36(tonumber(protein[link],16))
         *          protein[link] = ''
         *          n = n + 1
         *        else
         *          stick = sha256(stick)
         *        end
         *      until n == 9
         *      return v2
         *  end
         */
        
        HashMap<Long, String> protein = new HashMap();
        String                  stick   = Utils.subSHA256 (Utils.subSHA256(masterKey, 64), 64);
        long                    link    = 0;
        String                  address = "";
        
        int n = 0;
        for (; n < 9; n++)
        {
            protein.put ((long) n, stick.substring (0, 2));
            stick = Utils.subSHA256 (Utils.subSHA256 (stick, 64), 64);
        }
        
        n = 0;
        for (; n != 9;)
        {
            link = Long.parseLong (new BigInteger (stick.substring (2*n, 2 + (2*n)), 16).toString()) % 9;
            
            if (protein.get (link) != null && protein.get (link).length() > 0)
            {
                address = address + hextobase36_lua (new BigInteger (protein.get (link), 16));
                protein.put (link, "");
                
                n++;
            }
            else
            {
                stick = Utils.subSHA256 (stick, 64);
            }
        }
        
        return "k" + address;
    }
}
