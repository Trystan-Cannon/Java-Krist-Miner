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


public class Utils
{
    public static final String LAST_BLOCK_LINK   = "http://65.26.252.225/quest/dia/krist/index.php?lastblock";
    public static final String BALANCE_LINK_BASE = "http://65.26.252.225/quest/dia/krist/index.php?getbalance=";
    
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
        
        return balanceData != null ? balanceData.get (0) : "Connection time out.";
    }
    
    public static void submitSolution (String minerID, int nonce)
    {
        getPage ("http://65.26.252.225/quest/dia/krist/index.php?submitblock&address=" + minerID + "&nonce=" + nonce);
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
}
