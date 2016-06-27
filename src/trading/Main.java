/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

import com.ib.client.Contract;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.ib.client.TagValue;
import java.util.List;

/**
 *
 * @author paidforbyoptions
 */
public class Main {

    private static final int PORT_LIVE = 4001;
    private static final int PORT_PAPER = 4002;
    private static final int CLIENT_ID = 0;
    
    /**
     * @param args the command line arguments 
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {                                                                       

        Executive exec;        
        
        if (args.length == 0) {            
            System.out.println("ERROR: USAGE: ARGUMENT 1 = SOCKET PORT: 4001 FOR LIVE, 4002 FOR PAPER");            
            return;
        }        
        
        Path tickerFile = Paths.get("watchList.csv");
        
        switch (Integer.parseInt(args[0])) {
            case PORT_LIVE:
                exec = new Executive(tickerFile, PORT_LIVE, CLIENT_ID);
                break;
            case PORT_PAPER:
                exec = new Executive(tickerFile, PORT_PAPER, CLIENT_ID);
                break;            
            default:
                System.out.println("ERROR: FIRST ARGUMENT (PORT) MUST BE 4001 FOR LIVE OR 4002 FOR PAPER TRADING");
                return;
        }
        
        exec.importWatchlist();         
        exec.execute();
        
        
        
        
        
        
        
        //RealTimeDataStream dataStreamHandler = new DataStreamHandler(exec, exec.port, CLIENT_ID);
        //RealTimeDataStream dataStream2 = new DataStreamHandler(port, 2);
        //RealTimeDataStream dataStream3 = new DataStreamHandler(port, 3);
        
        
        
        // create a contract data structure
        
        /*
        Contract contract1 = new Contract ();
        contract1.m_symbol = "AAPL";
        contract1.m_exchange = "SMART";
        contract1.m_secType = "STK";
        contract1.m_currency = "USD";
        
        
        exec.dataStreamHandler.client.reqHistoricalData(CLIENT_ID, contract1, "20160620 00:00:00", "1 Y", "1 day", "TRADES", 1, 1, null);
        
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
        
        Vector<TagValue> mktDataOptions = new Vector<TagValue>();
        dataStreamHandler.client.reqMktData(1, contract1, null, false, mktDataOptions);        
        dataStreamHandler.client.reqMktData(2, contract2, null, false, mktDataOptions);     
        dataStreamHandler.client.reqMktData(3, contract3, null, false, mktDataOptions);     
        //dataStream2.client.reqMktData(1, contract2, null, false, mktDataOptions);        
        // Create a TagValue list
        */

        //dataStream.client.reqHistoricalData(1, contract, "20160416 16:00:00", "30 D", "1 day","BID_ASK", 1, 1, mktDataOptions);        
        //dataStream.client.reqMktData(2, contract, null, false, mktDataOptions);  
        //dataStream.client.reqContractDetails(1, contract);
        
        System.out.println("MAIN THREAD EXITING");;
    }
    
}
