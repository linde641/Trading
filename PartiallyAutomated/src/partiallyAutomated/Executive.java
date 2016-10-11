/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package partiallyAutomated;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author paidforbyoptions
 */
public class Executive implements Runnable {
    
    int     clientID;
    private final Path watchListPath;
    private final TradeData dataFileWrapper;
    public List<Trade> watchList;  
    public Map<String, Portfolio> portfolios; // maps acct Strings to portfolios. initialized after connection   
    public Map<Integer, Integer> permToTicker;
    public Map<Integer, Integer> tickerToOrder; // maps orderIDs to tickerIDs aka watchList index
    public DataStreamHandler dataStreamHandler;        
    
    public boolean flag = false;
    
    private Date date;
    private Timestamp ts;
    
    private boolean shutdown = false;    
    
    public Executive(Path watchListPath, int clientID, int port) throws IOException
    {
        this.watchListPath = watchListPath; 
        this.clientID = clientID;
        this.dataFileWrapper = new TradeData(watchListPath);                
        this.portfolios = new HashMap();
        this.permToTicker = new HashMap();
        this.tickerToOrder = new HashMap();    
        this.date = new Date();
        this.ts = new Timestamp(date.getTime());        
        
        this.watchList = dataFileWrapper.importTrades();
        
        /*Portfolio(s) not initialized until "ManagedAccounts" is called in response
        to the constructor below which creates socket connection etc.
        Done this way because in general I don't know how many accts/portfolios there
        will be yet. Should usually just be 1 but for scalability, handling however many
        */                
        
        
        connect(port);
        while( !flag ){ //DataStreamHandler sets flag to true when finished updating portfolios
            /* waiting for DataStreamHandler.managedAccounts to complete
            otherwise this constructor will return and execute() will begin
            before portfolio has been initialized.            
            */
        }
    
    }    
    
    @Override
    public void run() {
        execute();
    }

    public void shutdown()
    {
        shutdown = true;
    }
    
    private void connect(int port) // must be called to finish construction
    {
        this.dataStreamHandler = new DataStreamHandler(this, port, clientID);
    }
    
    private void disconnect()
    {
        dataStreamHandler.client.eDisconnect();
    }       
        
    public Timestamp getTimeStamp()
    {
        date = new Date();
        ts = new Timestamp(date.getTime());
        return ts;
    }    
    
    public String tsDateStr(Timestamp ts)
    {
        return ts.toString().substring(0, 9);
    }
    
    public String tsTimeStr(Timestamp ts)
    {
        return ts.toString().substring(11);
    }               

    
    private void execute() 
    {                
        portfolios.values().stream().forEach((p) -> {
            dataStreamHandler.client.reqAccountUpdates(true, p.acctCode);
        });        
                
        //dataStreamHandler.client.reqPositions();        
        
        Trade trade;
        
        for (int i = 0; i < watchList.size(); i++) {     
            trade = watchList.get(i);
            //trade.initOrder(clientID, trade.quantity());
            dataStreamHandler.client.reqMktData(i, trade.contract(), null, false, null);
            if (trade.secType().equals("OPT") && trade.stock() != null) {
                dataStreamHandler.client.reqMktData(i, trade.stock(), null, false, null);
            }
        }
                        
        while (!shutdown) {
            /*                        
                The EWrapper methods update the "currentPrice" variable in the trade objects that the 
                for loop below is looping through. 
            */
            
            for (int i = 0; i < watchList.size(); i++){
                
                trade = watchList.get(i);
                double currentPrice = trade.getPrice();
                
                if (!trade.isActive()) // hasn't triggered 
                {
                    // Each trade's current price is asynchronously updated by the eReader thread
                    
                    if ( ((currentPrice >= trade.entry()) && trade.isLong()
                            || currentPrice <= trade.entry() && trade.isShort() ) && currentPrice != 0) {// triggered
                        
                        trade.initOrder(clientID,dataStreamHandler.nextOrderID, trade.quantity());                        
                        dataStreamHandler.nextOrderID++;
                        
                        tickerToOrder.put(i, trade.order().m_orderId);
                        if (trade.enterTrade(dataStreamHandler.client) ){
                            System.out.println("Placed entry order for " + trade.toString());
                       //     trade.initOrder(clientID, trade.quantity()); // re-initializing the order for the cover later
                        }
                        else {
                            System.out.println("Entry order for " + trade.toString() + " failed");
                            
                        }
                    }
                } 
                else // trade is already active
                {
                /*// check for stop
                NOTE: later, need to implement more advanced causes of a trade exiting:
                rather than just a stop or target being hit, should exit if it 
                "feels weird", etc
                        */
                    if ( ((trade.isLong() && (currentPrice >= trade.target() || currentPrice <= trade.stop() ))
                            || (trade.isShort() && (currentPrice <= trade.target() || currentPrice >= trade.stop())) )
                            && currentPrice != 0)  {
                        // target is reached or getting stopped out
                        trade.initOrder(clientID,dataStreamHandler.nextOrderID, trade.quantity());
                        dataStreamHandler.nextOrderID++; // THIS MAY NEED SYNCHRONIZATION: ORDER ID IS UPDATED FROM EREADER THREAD  
                        tickerToOrder.put(i, trade.order().m_orderId);
                        if (trade.exitTrade(dataStreamHandler.client)) {
                            System.out.println("Placed exit order for " + trade.toString());
                        }
                        else {
                            System.out.println("Exit order for " + trade.toString() + " failed");
                        }
                    }                    
                    // else do nothing
                }
            }                        
        }
                
        disconnect();
        dataFileWrapper.exportTrades(watchList);
    }
}