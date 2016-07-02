/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/**
 *
 * @author paidforbyoptions
 */
public class Executive {

    // Data
    public int port;
    public int clientID;
    public Path tickerFile;        
    public List<Trade> watchList;  
    public Map<String, Portfolio> portfolios; // maps acct Strings to portfolios    
    public HashMap<Integer, Integer> orderIDtoTickerID; // maps orderIDs to tickerIDs aka watchList index
    DataStreamHandler dataStreamHandler;        
    

    public Executive(Path tickerFile, int port, int clientID)
    {        
        this.tickerFile = tickerFile; 
        this.clientID = clientID;
        this.watchList = new LinkedList(); 
        this.portfolios = new HashMap();
        
        /*Portfolio(s) not constructed until "ManagedAccounts" is called in response
        to the constructor below which creates socket connection etc.
        Done this way because in general I don't know how many accts/portfolios there
        will be yet. Should usually just be 1 but for scalability, handling however many
        */
        
        // connection happens in call stack started here
        this.dataStreamHandler = new DataStreamHandler(this, port, clientID);
    }    
    
    /**************************************************************************/
    /************************* Methods ****************************************/
    /**************************************************************************/
    
    public void importWatchlist()
    {        
        System.out.println("Getting trades from " + tickerFile.toAbsolutePath().toString());                
        
        /* the variables below should all be fields in the watchList file.
            Some are unused because they aren't in the file yet. These fields are saved
            as local variables because they are passed later into methods (mostly
            the trade constructor).
        */
        int quantity;
        String ticker, orderType;
        double entry, target, stop, rank, lmtPrce, auxPrice;      
        boolean isActive;                                             
    
        int i = 0;
    
        try (Scanner scanner = new Scanner(tickerFile)){                                               
            scanner.useDelimiter(",|\\r|\\n");                
            scanner.nextLine();            
            
            while (scanner.hasNextLine()) {                   
                Contract    contract = new Contract();
                Order       order = new Order();
                OrderState orderState = new OrderState();
                
                ticker = scanner.next();
                contract.m_symbol = ticker;
                entry = scanner.nextDouble();
                target = scanner.nextDouble();
                stop = scanner.nextDouble();
                quantity = scanner.nextInt();
                rank = scanner.nextDouble();                
                
                isActive = scanner.nextInt() != 0;
                
                contract.m_secType = scanner.next();
                
                if (contract.m_secType.equals("OPT")){
                    contract.m_expiry = scanner.next();
                    contract.m_strike = scanner.nextDouble();
                    contract.m_right = scanner.next();                                        
                    contract.m_conId = scanner.nextInt();                                        
                    contract.m_multiplier = "100";                    
                }                
                contract.m_currency = "USD";
                contract.m_exchange = "SMART";
                contract.m_includeExpired = false;         
                
                scanner.nextLine();
                
                Trade trade = new Trade(ticker, isActive, entry, target, stop,
                        rank, contract, order, orderState, quantity, clientID);
                /* 
                NOTE: this ^ constructor calls a private method to set the main 
                order fields such as "action" etc.
                */
                watchList.add(trade);
                System.out.println(watchList.get(i).toString());
                i++;                                
            }                                    
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        finally
        {
            
        }
    }            
    
    
    public void execute() 
    {
        for (Portfolio p : portfolios.values()) {
            dataStreamHandler.client.reqAccountUpdates(true, p.acctCode);            
        }
        
        dataStreamHandler.client.reqPositions();
        
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            System.out.println("Nigger");
        }
        System.exit(0);
        
        for (int i = 0; i < watchList.size(); i++) {
            dataStreamHandler.client.reqMktData(i, watchList.get(i).contract, null, false, null);                                
        }
                        
        while (true) {
            /*                        
                The EWrapper methods update the "currentPrice" variable in the trade objects that the 
                for loop below is looping through. 
            */
            
            for (int i = 0; i < watchList.size(); i++){
                
                Trade trade = watchList.get(i);
                double currentPrice = trade.getPrice();
                
                if (!trade.isActive) // hasn't triggered 
                {
                    // assuming here that each trade's current price is asynchronously updated by the client thread
                    
                    if ((currentPrice >= trade.entry && trade.isLong)
                            || currentPrice <= trade.entry && trade.isShort) {// triggered
                        
                        trade.order.m_orderId = dataStreamHandler.nextOrderID;
                        dataStreamHandler.nextOrderID++; // THIS MAY NEED SYNCHRONIZATION: ORDER ID IS UPDATED FROM EREADER THREAD
                        /* 
                        still need to set the order's price, permId, etc before submitting
                        might want to change the order type here. It's initialized as LMT
                        */
                        trade.order.m_lmtPrice = trade.last;
                        dataStreamHandler.client.placeOrder(trade.order.m_orderId, trade.contract, trade.order);
                        System.out.println("Placed order for " + trade.toString());  
                        trade.isActive = true;
                    }
                } 
                else // trade is already active
                {
                        // check for stop
        //                    NOTE: later, need to implement more advanced causes of a trade exiting:
        //                    rather than just a stop or target being hit, should exit if it 
        //                            "feels weird", etc

                    if (trade.isLong
                            && (currentPrice >= trade.target || currentPrice <= trade.stop)) {
                        // target is reached or getting stopped out
                        trade.exitTrade();
                    } 
                    else if (trade.isShort
                            && (currentPrice <= trade.target || currentPrice >= trade.stop)) {
                        trade.exitTrade();
                    }
                    // else do nothing
                }
            }        
        }
    }
}