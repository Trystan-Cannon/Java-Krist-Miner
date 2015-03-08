package krist.miner;

import gui.ManagerGUI;

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

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;


public class Utils
{
    /**
     * Miner configuration file format:
     * Lines that start with '#' are comments.
     * Following this, we're looking for the field: coreLimit=...
     * 
     * The default coreLimit is 1.
     */
    private static final String CONFIG_FILE_PATH = "config.txt";
    
    private static final String KRIST_SYNC_LINK = getPage("https://raw.githubusercontent.com/BTCTaras/kristwallet/master/staticapi/syncNode").get(0) + "?";
    private static final String LAST_BLOCK_LINK = KRIST_SYNC_LINK + "lastblock";
    private static final String BALANCE_LINK_BASE = KRIST_SYNC_LINK + "getbalance=";
    
    public static String getLastBlock()
    {
        ArrayList<String> lastBlockPageData = null;
        
        while (lastBlockPageData == null)
        {
            lastBlockPageData = getPage(LAST_BLOCK_LINK);
        }
        
        return lastBlockPageData.get(0);
    }
    
    public static String getBalance(String userAddress)
    {
        ArrayList<String> balanceData = getPage(BALANCE_LINK_BASE + userAddress);
        return balanceData == null ? null : balanceData.get(0);
    }
    
    public static void submitSolution(String minerID, long nonce)
    {
        getPage(KRIST_SYNC_LINK + "submitblock&address=" + minerID + "&nonce=" + nonce);
    }
    
    public static String subSHA256(String data, int endIndex)
    {
        return Hashing.sha256().hashString(data, Charsets.UTF_8).toString().substring(0, endIndex);
    }
    
    public static boolean isMinerValid(String minerID)
    {
        //Look, it's not 10 characters! DEFINITELY VALID! (Taras's server doesn't catch this.)
        if(minerID.length() != 10) return false;
        
        ArrayList<String> minerValidity = getPage(BALANCE_LINK_BASE + minerID);
        
        // Error retrieving page data.
        return minerValidity != null && !minerValidity.isEmpty();
    }
    
    public static ArrayList<String> getPage(String url)
    {
        try
        {
            URL lastBlockURL = new URL(url);
            InputStream pageInputStream = lastBlockURL.openStream();
            BufferedReader pageReader = new BufferedReader(new InputStreamReader(pageInputStream));
            
            ArrayList<String> lines = new ArrayList<String>();
            String line;
            
            while ((line = pageReader.readLine()) != null)
            {
                lines.add(line);
            }
            
            return lines;
        }
        catch (MalformedURLException malformedException)
        {
            System.err.println(malformedException.getMessage());
        }
        catch (IOException ioException)
        {
            System.err.println(ioException.getMessage());
        }
        
        return null;
    }
    
    public static boolean createConfigurationFile()
    {
        File configurationFile = new File(CONFIG_FILE_PATH);
        
        // Even if we're creating the file, let's be safe and make sure
        // that it doesn't already exist.
        if (!configurationFile.exists())
        {
            try
            {
                configurationFile.createNewFile();
                
                FileWriter writerHandle = new FileWriter(configurationFile.getAbsolutePath());
                BufferedWriter writer = new BufferedWriter(writerHandle);
                
                writer.write("# This is the maximum allowed number of cores that the miner can utilize.");
                writer.newLine();
                writer.write("# The default value is 1. The maxmimum is 8. If you choose to use more than");
                writer.newLine();
                writer.write("# the recommended number of cores, your computer stands the risk of a thermal\n");
                writer.newLine();
                writer.write("# shutdown or, more  dangerously, damage by overheating. You have been warned.");
                writer.newLine();
                writer.write("# I, Trystan Cannon, bare no responsibility for your actions.");
                writer.newLine();
                
                writer.write("coreLimit=" + ManagerGUI.DEFAULT_MAX_CORE_LIMIT);
                writer.close();
                
                return true;
            }
            catch (IOException failureReport)
            {
                System.err.println("Failed to create new configuration file.");
            }
        }
        
        return false;
    }
    
    public static int getConfiguredCoreLimit()
    {
        File configurationFile = new File(CONFIG_FILE_PATH);
        
        // Make sure that our file exists. If not, then we'll create it.
        if (configurationFile.exists() && !configurationFile.isDirectory())
        {
            try
            {
                FileReader readerHandle = new FileReader(configurationFile.getAbsolutePath());
                BufferedReader reader = new BufferedReader(readerHandle);
                
                // Read lines from the file until we hit the core limit.
                String line;
                while ((line = reader.readLine()) != null && !line.startsWith("coreLimit="))
                {}
                
                // Convert the limit to a valid integer.
                try
                {
                    int configuredCoreLimit = Integer.parseInt(line.substring(("coreLimit=").length()));
                    
                    // Make sure the core limit is within the max and min
                    // values.
                    if (configuredCoreLimit >= ManagerGUI.DEFAULT_MAX_CORE_LIMIT && configuredCoreLimit <= ManagerGUI.MAX_CORE_LIMIT)
                    {
                        reader.close();
                        return configuredCoreLimit;
                    }
                }
                catch (Exception conversionFailureReport)
                {
                    System.err.println("Failed to convert configured core limit to integer.");
                }
            }
            catch (IOException failureReport)
            {
                System.err.println("Failed to read configuration core limit from existing file.");
            }
        }
        else
        {
            createConfigurationFile();
        }
        
        return ManagerGUI.DEFAULT_MAX_CORE_LIMIT;
    }
    
    /**
     * Generates a Krist v2 address using the original algorithm.
     * This was direclty ported from the KristWallet source (release 8).
     * @param key
     *  KristWallet password
     * @return Krist address
     */
    public static String generateAddress(String key) {
        String masterKey = Utils.subSHA256("KRISTWALLET" + key, 64) + "-000";
        
        HashMap<Long, String> protein = new HashMap<Long, String>(); //local protein = {}
        String stick = subSHA256(subSHA256(masterKey, 64), 64);   //local stick = sha256(sha256(key))
        long link = 0;
        String v2 = "k"; //All keys start with 'k'. Bad design in my opinion, but whatever.
        
        for (long n = 0; n < 9; n++) { //repeat...until n == 9
            protein.put(n, stick.substring(0, 2)); //if n < 9 then protein[n] = string.sub(stick,0,2)
            stick = subSHA256(subSHA256(stick, 64), 64); //stick = sha256(sha256(stick)) end
        }
        
        int n = 0;
        while (n < 9) {
            link = Long.parseLong(new BigInteger(stick.substring(2 * n, 2 * n + 2), 16).toString()) % 9; //link = tonumber( string.sub( stick,1+(2*n),2+(2*n) ),16 ) % 9
            if (protein.get(link).length() != 0) { //if string.len(protein[link]) ~= 0 then
                v2 += hextobase36(new BigInteger(protein.get(link), 16)); //v2 = v2 .. hextobase36(tonumber(protein[link],16))
                protein.put(link, ""); //protein[link] = ''
                n++;
            } else stick = subSHA256(stick, 64); //stick = sha256(stick)
        }
        return v2;
    }
    
    //Thanks Grim Reaper
    private static String hextobase36(BigInteger number) {
        for (int i = 6; i <= 251; i += 7)
            if (number.compareTo(new BigInteger("" + i)) <= 0)
                if (i <= 69) return (char) ('0' + (i - 6) / 7) + "";
                else         return (char) ('a' + ((i - 76) / 7)) + "";
        return "e";
    }
}
