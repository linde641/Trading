/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
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
public class Execution {

    // Data
    
    public Path tickerFile;      
    public List<Trade> watchList;    
    RealTimeDataStream dataStream;
    
    /**************************************************************************/
    /************************* Constructors ***********************************/
    /**************************************************************************/
    
    public Execution(Path tickerFile, RealTimeDataStream dataStream)
    {
        this.tickerFile = tickerFile;        
        this.watchList = new LinkedList();         
        this.dataStream = dataStream;
    }
    
    
    /**************************************************************************/
    /************************* Methods ****************************************/
    /**************************************************************************/
    
    public void execute() {
        float currentPrice;

        for (Trade trade : watchList) {

            //currentPrice = trade.updatePrice();
            
            dataStream.client.reqMktData(dataStream.nextOrderID, trade.contract, null, false, null);
            
            
        }
            
            
        while(true) {
        for (Trade trade : watchList){
            if (!trade.isActive) // hasn't triggered 
            {
                // assuming here that each trade's current price is asynchronously updated by the client thread
                
                if ((trade.currentPrice >= trade.entry && trade.isLong)
                        || (trade.currentPrice <= trade.entry && trade.isShort)) {// triggered
                    trade.enterTrade();
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
    
    
    
    public void importWatchlist()
    {        
        System.out.println("Getting trades from " + tickerFile.toAbsolutePath().toString());
        
        int i = 0;
        
        String ticker;
        float entry;
        float target;
        float stop;
        float rank;          
        boolean isActive;
                                            
    // COMBOS
        String comboLegsDescrip; 
        Vector<ComboLeg> comboLegs = new Vector();

    // delta neutral
        UnderComp underComp;        
        
        try (Scanner scanner = new Scanner(tickerFile)){                                    
           
            /*            
            Trade format:
                Ticker, Entry, Target, Stop, Rank, Active (T/F)
            */
                                    
            while (scanner.hasNextLine()) {   
                Contract contract = new Contract();
                
                ticker = scanner.next();
                entry = scanner.nextFloat();
                target = scanner.nextFloat();
                stop = scanner.nextFloat();
                rank = scanner.nextFloat();                
                
                if (scanner.nextInt() == 0){
                    isActive = false;
                }
                else {
                    isActive = true;
                }
                
                contract.m_secType = scanner.next();
                if (contract.m_secType.equals("OPT")){
                    contract.m_expiry = scanner.next();
                    contract.m_strike = scanner.nextDouble();
                    contract.m_right = scanner.next();
                    contract.m_primaryExch = scanner.next();
                    contract.m_exchange = "SMART";
                    contract.m_multiplier = "100";
                }
                contract.m_currency = "USD";
                contract.m_includeExpired = false;
                contract.m_comboLegs = comboLegs;
                
                //scanner.nextLine();
                
                
                
                
                watchList.add(new Trade(ticker, isActive, entry, target, stop, rank, contract));
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
    
}
