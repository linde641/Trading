/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.TagValue;
import com.ib.client.UnderComp;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

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
    RealTimeDataStream dataStream;    
    
    /**************************************************************************/
    /************************* Constructors ***********************************/
    /**************************************************************************/
    
    /**
     *
     * @param tickerFile
     * @param port
     * @param clientID
     */
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
        
        int i = 0;
        
        String ticker;
        double entry, target, stop, rank;      
        boolean isActive;
                                            
    // COMBOS
        String comboLegsDescrip; 
        Vector<ComboLeg> comboLegs = new Vector();

    // delta neutral
        UnderComp underComp;        
        
        try (Scanner scanner = new Scanner(tickerFile)){                                               
            scanner.useDelimiter("\t");                
            String firstLine = scanner.nextLine();            
            
            while (scanner.hasNextLine()) {                   
                Contract    contract = new Contract();
                Order       order = new Order(); // still need to populate this
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
                contract.m_comboLegs = comboLegs;
                
                scanner.nextLine();
                                                                
                watchList.add(new Trade(ticker, isActive, entry, target, stop, rank, contract, order));
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
        //float currentPrice;
        
        for (int i = 0; i < watchList.size(); i++) {
            // start the price data streams            
            Vector<TagValue> mktDataOptions = new Vector<TagValue>();
            dataStream.client.reqMktData(i, watchList.get(i).contract, null, false, mktDataOptions);                                
        }
                        
        while (true) {
            /*
                In the above for loop, each socket will request data from the server. Then each associated 
                EReader thread will wait for the data and call the associated EWrapper methods on their parent 
                datastream object as the data comes in. So the EWrapper methods need to probably store the data
                in a data structure that is available to this thread, which I think is trivial because there is 
                only one process (I think). 
            
                Then, this loop basically just needs to have a mechanism to poll that data structure or something
                and then enter/exit trades using the datastream objects as needed. 
            
            UPDATE: 
                The EWrapper methods can simply update the "currentPrice" variable in the trade objects that the 
                for loop below is looping through. This should be possible but will require mutex locks. Then the below 
                code would already basically work. 
            */
            
            for (int i = 0; i < watchList.size(); i++){
                
                Trade trade = watchList.get(i);
                
                if (!trade.isActive) // hasn't triggered 
                {
                    // assuming here that each trade's current price is asynchronously updated by the client thread

                    if ((trade.currentPrice >= trade.entry && trade.isLong)
                            || (trade.currentPrice <= trade.entry && trade.isShort)) {// triggered
                        //Need to generate the order object here or somewhere at least
                        //dataStream.client.placeOrder(orderID, contract, order);
                        System.out.println("Placing order for " + trade.toString());
                    }
                } 
                else // trade is already active
                {
                        // check for stop
        //                    NOTE: later, need to implement more advanced causes of a trade exiting:
        //                    rather than just a stop or target being hit, should exit if it 
        //                            "feels weird", etc

                    if (trade.isLong
                            && (trade.currentPrice >= trade.target || trade.currentPrice <= trade.stop)) {
                        // target is reached or getting stopped out
                        trade.exitTrade();
                    } 
                    else if (trade.isShort
                            && (trade.currentPrice <= trade.target || trade.currentPrice >= trade.stop)) {
                        trade.exitTrade();
                    }
                    // else do nothing
                }
            }

        // might need a delay here so as to not overload api data limit
        }
    }
}