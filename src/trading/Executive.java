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
    public HashMap<Integer, Order> orders;
    RealTimeDataStream dataStream;        
    

    public Executive(Path tickerFile, int port, int clientID)
    {
        this.dataStream = new RealTimeDataStream(this, port, clientID);
        this.tickerFile = tickerFile;           
        this.watchList = new LinkedList(); 
        
    }    
    
    /**************************************************************************/
    /************************* Methods ****************************************/
    /**************************************************************************/
    
    public void importWatchlist()
    {        
        System.out.println("Getting trades from " + tickerFile.toAbsolutePath().toString());                
        
        String ticker;
        double entry, target, stop, rank;      
        boolean isActive;
                                            
    // COMBOS
        //String comboLegsDescrip; 
        //Vector<ComboLeg> comboLegs = new Vector();

    // delta neutral              
    
        int i = 0;
    
        try (Scanner scanner = new Scanner(tickerFile)){                                               
            scanner.useDelimiter("\t");                
            scanner.nextLine();            
            
            while (scanner.hasNextLine()) {                   
                Contract    contract = new Contract();
                Order       order = new Order(); // still need to populate this
                OrderState orderState = new OrderState();
                
                ticker = scanner.next();
                contract.m_symbol = ticker;
                entry = scanner.nextDouble();
                target = scanner.nextDouble();
                stop = scanner.nextDouble();
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
                //contract.m_comboLegs = comboLegs;
                
                scanner.nextLine();
                                                                
                watchList.add(new Trade(ticker, isActive, entry, target, stop,
                        rank, contract, order, orderState));
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
        for (int i = 0; i < watchList.size(); i++) {
            // start the price data streams            
            //Vector<TagValue> mktDataOptions = new Vector<TagValue>();
            dataStream.client.reqMktData(i, watchList.get(i).contract, null, false, null);                                
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
                        //Need to generate the order object here or somewhere at least
                        trade.order.m_orderId = dataStream.nextOrderID;
                        dataStream.nextOrderID++; // THIS MAY NEED SYNCHRONIZATION: ORDER ID IS UPDATED FROM EREADER THREAD
                        dataStream.client.placeOrder(trade.order.m_orderId, trade.contract, trade.order);
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