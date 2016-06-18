/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.ib.client.Contract;
import com.ib.client.TagValue;
import java.util.Vector;
/**
 *
 * @author paidforbyoptions
 */
public class Main {

    private static final int PORT = 4001;
    private static final int CLIENT_ID = 0;
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {        
     
        Path tickerFile = Paths.get("watchList.txt");                
        
        // get watchlist from file
       
        
        // connect to IB Gateway
        
        //RealTimeDataStream dataStream = new RealTimeDataStream(PORT, CLIENT_ID);
        //RealTimeDataStream dataStream2 = new RealTimeDataStream(port, 2);
        //RealTimeDataStream dataStream3 = new RealTimeDataStream(port, 3);
        
        
        Executive exec = new Executive(tickerFile, PORT, CLIENT_ID);
        exec.importWatchlist();         
        exec.execute();
        
        
        
        
        
        
        
        
        
        
        
        // create a contract data structure
        
        Contract contract1 = new Contract ();
        contract1.m_symbol = "AAPL";
        contract1.m_exchange = "SMART";
        contract1.m_secType = "STK";
        contract1.m_currency = "USD";

        Contract contract2 = new Contract ();
        contract2.m_symbol = "GOOGL";
        contract2.m_exchange = "SMART";
        contract2.m_secType = "STK";
        contract2.m_currency = "USD";                
        
        Contract contract3 = new Contract ();
        contract3.m_conId = 232384443;
        contract3.m_symbol = "AAPL";
        contract3.m_exchange = "SMART";
        contract3.m_secType = "OPT";
        contract3.m_currency = "USD";
        contract3.m_expiry = "20160618";
        contract3.m_right = "CALL";
        contract3.m_strike = 100.00;
        contract3.m_multiplier = "100";
        /*
        Vector<TagValue> mktDataOptions = new Vector<TagValue>();
        dataStream.client.reqMktData(1, contract1, null, false, mktDataOptions);        
        dataStream.client.reqMktData(2, contract2, null, false, mktDataOptions);     
        dataStream.client.reqMktData(3, contract3, null, false, mktDataOptions);     
        //dataStream2.client.reqMktData(1, contract2, null, false, mktDataOptions);        
        // Create a TagValue list
        */

        //dataStream.client.reqHistoricalData(1, contract, "20160416 16:00:00", "30 D", "1 day","BID_ASK", 1, 1, mktDataOptions);        
        //dataStream.client.reqMktData(2, contract, null, false, mktDataOptions);  
        //dataStream.client.reqContractDetails(1, contract);
        
        System.out.println("home thread exiting");

    }
    
}
