/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marketdata;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aronlindell
 */
public class Executive implements Runnable {
    
    int clientID;
    public Path watchListPath;
    List<Contract> contracts;
    DataStreamHandler dataStreamHandler;
    BlockingQueue <L2Data> L2DataQueue;
    List<File> files;
    List<BufferedOutputStream> streams;
    
    public Calendar calendar;
    public Date date;
    public Timestamp ts;    
    
    boolean shutdown = false;
    
    
    public Executive(Path watchListPath, int clientID, int port)
    {        
        this.clientID = clientID;
        this.contracts = new LinkedList();
        this.watchListPath = watchListPath;        
        L2DataQueue = new LinkedBlockingQueue<>();
        files = new ArrayList<>();
        streams = new ArrayList<>();
        
        calendar = Calendar.getInstance();
        date = new Date();
        ts = new Timestamp(date.getTime());
        
        importWatchList();
        
        connect(port);        
    }    
    
    public Timestamp getTimeStamp()
    {
        //calendar = Calendar.getInstance();
        //date = calendar.getTime();
        date = new Date();
        ts = new Timestamp(date.getTime());
        return ts;
    }
    
    private void importWatchList()
    {        
        System.out.println("Getting tickers from " + watchListPath.toAbsolutePath().toString());                    
        String dataDir = Paths.get(".").toAbsolutePath().normalize().toString().concat("/Database/");
        
        try (Scanner scanner = new Scanner(watchListPath)){                                               
            scanner.useDelimiter(",|\\r|\\n");                
            scanner.nextLine();            
            
            while (scanner.hasNextLine()) {              
                
                Contract    contract = new Contract();                                
                contract.m_symbol = scanner.next();                    
                String exchange = scanner.next();
                contract.m_secType = "STK";                       
                contract.m_currency = "USD";
                contract.m_exchange = exchange;                
                contract.m_includeExpired = false;         
                
                contracts.add(contract);
                
                String filename = dataDir.concat(contract.m_symbol); 
                String str = ts.toString().substring(0, 19).replace(" ", "_").replace(":", "_");
                filename = filename.concat("_" + str + ".txt"); // ex: SPY_2016-07-12_hhmmss.ns.txt
                File file = new File(filename);
                files.add(file);
                
                scanner.nextLine();
            }                                    
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        finally
        {
            
        }
    }
    
    
    private void connect(int port) // must be called to finish construction
    {
        this.dataStreamHandler = new DataStreamHandler(this, port, clientID);
    }
    
    private void disconnect()
    {
        dataStreamHandler.client.eDisconnect();
    }
    
    public void execute() throws IOException
    {
        FileOutputStream stream;
        BufferedOutputStream bufStream;
        L2Data entry;                
        
        for (int i = 0; i < files.size(); i++) {
            File f = files.get(i);
            if ( !f.exists() ) {
                f.createNewFile();
            }
            stream = new FileOutputStream(f);
            bufStream = new BufferedOutputStream(stream);
            streams.add(bufStream);
        }
        
        for (int i = 0; i < contracts.size(); i++) {            
            dataStreamHandler.client.reqMktData(i, contracts.get(i), null, false, null);
            dataStreamHandler.client.reqMktDepth(i, contracts.get(i), 7, null);
            //dataStreamHandler.client.reqContractDetails(i, contracts.get(i));
        }                
                
        while(!shutdown) {
            if ( !L2DataQueue.isEmpty()) {                
                entry = L2DataQueue.poll();
                //System.out.println(entry.toString());                
                bufStream = streams.get(entry.tickerID);                
                bufStream.write(entry.toString().getBytes());                
            }
        }

        for (int i = 0; i < streams.size(); i++) {
            streams.get(i).flush();
        }     
        
        disconnect();  
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (IOException ex) {
            //Logger.getLogger(Executive.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Execute.run() Failed: " + ex.toString());
        }
    }
    
    
}
