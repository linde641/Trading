/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package partiallyAutomated;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author aronlindell
 */
public class TradeData {
    
    private final Path path;
    private final File file;
    private BufferedWriter fw;
    private final Scanner scanner;        
    
    private enum Column {
    
        TICKER      (0, "Ticker"),
        ENTRY       (1, "Entry"),
        TARGET      (2, "Target"),
        STOP        (3, "Stop"),
        QUANTITY    (4, "Quantity"),
        RANK        (5, "Rank"),
        ISACTIVE    (6, "IsActive"),
        SECTYPE     (7, "SecType"),
        CONID       (8, "ConID"),
        STOCKID     (9, "StockID"),
        EXPIRY      (10, "Expiry"),
        STRIKE      (11, "Strike"),
        RIGHT       (12, "Right");                
        
        private final int index;
        private final String str;
        Column (int index, String str)
        {
            this.index = index;
            this.str = str;
        }
        
        String str () { return this.str; };
    }    

    
    public TradeData(Path path) throws IOException
    {
        this.path = path;
        this.file = path.toAbsolutePath().toFile();        
        this.fw = new BufferedWriter(new FileWriter(file, true));                        
        this.scanner = new Scanner(file);

    }
    
    
    public List<Trade> importTrades()
    {        
        System.out.println("Getting data from " + path.toAbsolutePath().toString());                       
    
        String  ticker;
        double  entry;
        double  target;
        double  stop;
        int     quantity;
        double  rank;
        boolean isActive;
        String  secType;
        int     stockID;
        int     conID;
        String  expiry;
        double  strike;
        String  right;
        
           
        List<Trade> watchList = new LinkedList();
        Order       order = new Order();
        OrderState  orderState = new OrderState(); 
        Position    position = new Position();
        Contract    contract;
        Contract    stock;
        
        scanner.useDelimiter(",|\\r|\\n");                
        scanner.nextLine();// skip header line

        while (scanner.hasNextLine()) {                          
            ticker = scanner.next();            
            entry = scanner.nextDouble();
            target = scanner.nextDouble();
            stop = scanner.nextDouble();
            quantity = scanner.nextInt();
            rank = scanner.nextDouble();                
            isActive = scanner.nextBoolean();
            secType = scanner.next();                                              
            conID = scanner.nextInt();  
            stockID = scanner.nextInt();
            
            if (secType.equals("OPT")){
                expiry = scanner.next();
                strike = scanner.nextDouble();
                right = scanner.next();                                
                
                contract = new Contract();
                stock = new Contract();
                
                contract.m_symbol = ticker;
                contract.m_conId = conID;
                contract.m_secType = secType;                                    
                contract.m_currency = "USD";
                contract.m_exchange = "SMART";
                contract.m_includeExpired = false;
                contract.m_expiry = expiry;
                contract.m_strike = strike;
                contract.m_right = right;                
                contract.m_multiplier = "100";    
                
                stock.m_symbol = ticker;
                stock.m_conId = stockID;
                stock.m_secType = "STK";
                stock.m_currency = "USD";
                stock.m_exchange = "SMART";
                stock.m_includeExpired = false;
            }
            else if (secType.equals("STK")){ // STK
                contract = null;
                stock = new Contract();
                
                stock.m_symbol = ticker;
                stock.m_conId = stockID;
                stock.m_secType = "STK";
                stock.m_currency = "USD";
                stock.m_exchange = "SMART";
                stock.m_includeExpired = false;                
            }
            else {
                contract = null;
                stock = null;
                System.out.println(String.format("Security type invalid: %s%n", secType));
                System.exit(-1);
            }

            scanner.nextLine();
            
            Trade trade = new Trade(ticker, entry, target, stop, quantity, 
                    rank, isActive, secType, contract, stock, position, order, orderState);

            watchList.add(trade);
            System.out.println(trade.toString());            
        }
        
        return watchList;
    }    
    
    
    public void exportTrades(List<Trade> watchList)
    {
        String  ticker;
        double  entry;
        double  target;
        double  stop;
        int     quantity;
        double  rank;
        boolean isActive;
        String  secType;
        int     conID;
        int     stockID;
        String  expiry;
        double  strike;
        String  right;                     
        
        String row = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                Column.TICKER.str(), Column.ENTRY.str(), Column.TARGET.str(), Column.STOP.str(), Column.QUANTITY.str(),
                Column.RANK.str(), Column.ISACTIVE.str(), Column.SECTYPE.str(), Column.CONID.str(), Column.STOCKID.str(), 
                Column.EXPIRY.str(), Column.STRIKE.str(), Column.RIGHT.str());
        
        try {
            file.delete();
            file.createNewFile();  
            fw = new BufferedWriter(new FileWriter(file, true));
            fw.write(row);
            
            for (Trade trade : watchList) {
                ticker      = trade.ticker();
                entry       = trade.entry();
                target      = trade.target();
                stop        = trade.stop();
                quantity    = trade.quantity();
                rank        = trade.rank();
                isActive    = trade.isActive();
                secType     = trade.secType();
                conID       = trade.option().m_conId;                

                if (secType.equals("OPT")) {
                    expiry  = trade.contract().m_expiry;
                    strike  = trade.contract().m_strike;
                    right   = trade.contract().m_right;
                    row = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n", 
                        ticker, String.valueOf(entry), String.valueOf(target), String.valueOf(stop), String.valueOf(quantity),
                        String.valueOf(rank), String.valueOf(isActive), secType, String.valueOf(conID), expiry, String.valueOf(strike), right);
                }   
                else {
                    row = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n", 
                        ticker, String.valueOf(entry), String.valueOf(target), String.valueOf(stop), String.valueOf(quantity),
                        String.valueOf(rank), String.valueOf(isActive), secType, String.valueOf(conID), "", "", "");
                }
                
                fw.write(row);
            }            
            fw.close();
            
        } catch (IOException ex) {
            System.err.format("%s error in exportTrades%n", ex);
        }                    
    }               
}
