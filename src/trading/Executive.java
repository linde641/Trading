/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author paidforbyoptions
 */
public class Executive {
    
    int     clientID;
    public  Path watchListPath;        
    public  List<Trade> watchList;  
    public  Map<String, Portfolio> portfolios; // maps acct Strings to portfolios. initialized after connection   
    public  Map<Integer, Integer> orderIDtoPermID;
    public  HashMap<Integer, Integer> orderIDtoTickerID; // maps permIDs to tickerIDs aka watchList index
    DataStreamHandler dataStreamHandler;        
    
    
    public Executive(Path watchListFile, int clientID, int port)
    {        
        this.watchListPath = watchListFile; 
        this.clientID = clientID;
        this.watchList = new LinkedList(); 
        this.portfolios = new HashMap();
        this.orderIDtoPermID = new HashMap();
        this.orderIDtoTickerID = new HashMap();
        
        /*Portfolio(s) not constructed until "ManagedAccounts" is called in response
        to the constructor below which creates socket connection etc.
        Done this way because in general I don't know how many accts/portfolios there
        will be yet. Should usually just be 1 but for scalability, handling however many
        */                
        
        importWatchList();    
        connect(port);
        while(portfolios.isEmpty()){ // this needs to be fixed. !isEmpty doesn't mean it's actually done updating.
            /* waiting for DataStreamHandler.managedAccounts to complete
            otherwise this constructor will return and execute() will begin
            before portfolio has been initialized.
            */
        }
    }    
    
    /**************************************************************************/
    /************************* Methods ****************************************/
    /**************************************************************************/      
    
    private void connect(int port) // must be called to finish construction
    {
        this.dataStreamHandler = new DataStreamHandler(this, port, clientID);
    }
    
    
    private void importWatchList()
    {        
        System.out.println("Getting trades from " + watchListPath.toAbsolutePath().toString());                
        
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
    
        try (Scanner scanner = new Scanner(watchListPath)){                                               
            scanner.useDelimiter(",|\\r|\\n");                
            scanner.nextLine();            
            
            while (scanner.hasNextLine()) {              
                
                Contract    contract = new Contract();
                Order       order = new Order();
                OrderState  orderState = new OrderState(); 
                Position    position = new Position();
                
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
                
                //position.contract(contract);
                Trade trade = new Trade(ticker, isActive, entry, target, stop,
                        rank,contract, position, order, orderState, quantity, clientID);

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
    
    /*
    public void updateWatchListFile(int index, int column, Object value)
    {        
        try {
            //RandomAccessFile file = new RandomAccessFile(watchListPath.toFile(), "rw");
            FileReader fr = new FileReader(watchListPath.toFile());
            BufferedReader reader = new BufferedReader(fr);
            FileWriter fw = new FileWriter(watchListPath.toFile());
            BufferedWriter writer = new BufferedWriter(fw);
            
            for (int i = 0; i <= watchList.size(); i++){ //using <= to skip file header
                reader.readLine();
            }
            int c;
            for (int i = 0; i < column; i++){
                while ( !String.valueOf(c = reader.read()).equals(",") ){
                    // scan till comma
                }                
            }
            writer.w
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Executive.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Executive.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    */
    
    public void execute() 
    {        
        for (Portfolio p : portfolios.values()) {
            dataStreamHandler.client.reqAccountUpdates(true, p.acctCode);            
        }        
                
        //dataStreamHandler.client.reqPositions();
        
        /*
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            System.out.println("Nigger");
        }
        System.exit(0);
        */
        
        Trade trade;
        
        for (int i = 0; i < watchList.size(); i++) {
            trade = watchList.get(i);
            trade.initOrder(clientID, trade.quantity());
            dataStreamHandler.client.reqMktData(i, trade.contract, null, false, null);                                
        }
                        
        while (true) {
            /*                        
                The EWrapper methods update the "currentPrice" variable in the trade objects that the 
                for loop below is looping through. 
            */
            
            for (int i = 0; i < watchList.size(); i++){
                
                trade = watchList.get(i);
                double currentPrice = trade.getPrice();
                
                if (!trade.isActive) // hasn't triggered 
                {
                    // assuming here that each trade's current price is asynchronously updated by the client thread
                    
                    if ( ((currentPrice >= trade.entry && trade.isLong)
                            || currentPrice <= trade.entry && trade.isShort) && currentPrice != 0) {// triggered
                        
                        trade.order.m_orderId = dataStreamHandler.nextOrderID;
                        dataStreamHandler.nextOrderID++; // THIS MAY NEED SYNCHRONIZATION: ORDER ID IS UPDATED FROM EREADER THREAD
                        // the above two lines should really be made atomic so the used orderID can't be used twice
                        orderIDtoTickerID.put(trade.order.m_orderId, i);            
                        if (trade.enterTrade(dataStreamHandler.client) ){
                            System.out.println("Placed entry order for " + trade.toString());
                            trade.initOrder(clientID, trade.quantity()); // re-initializing the order for the cover later
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
                    if ( ((trade.isLong && (currentPrice >= trade.target || currentPrice <= trade.stop))
                            || (trade.isShort && (currentPrice <= trade.target || currentPrice >= trade.stop)) )
                            && currentPrice != 0)  {
                        // target is reached or getting stopped out
                        trade.order.m_orderId = dataStreamHandler.nextOrderID;
                        dataStreamHandler.nextOrderID++; // THIS MAY NEED SYNCHRONIZATION: ORDER ID IS UPDATED FROM EREADER THREAD                        
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
    }
}