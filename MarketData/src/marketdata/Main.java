/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marketdata;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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
        int port;        
        
        
        if (args.length == 0) {            
            System.out.println("ERROR: USAGE: ARGUMENT 1 = SOCKET PORT: 4001 FOR LIVE, 4002 FOR PAPER");            
            return;
        }                        
        
        switch (Integer.parseInt(args[0])) {
            case PORT_LIVE:
                port = PORT_LIVE;
                break;
            case PORT_PAPER:
                port = PORT_PAPER;                
                break;            
            default:
                System.out.println("ERROR: FIRST ARGUMENT (PORT) MUST BE 4001 FOR LIVE OR 4002 FOR PAPER TRADING");
                return;
        }
        
        Path watchListFile = Paths.get("watchList.csv");
        exec = new Executive(watchListFile, CLIENT_ID, port); // imports watchList here        
        //exec.execute();
        new Thread(exec).start();
        Scanner sc = new Scanner(System.in);
        System.out.println("Type 'exit' to exit program");
               
        while(true) {
            String input = sc.next(); 
            if (input.equals("exit") || input.equals("Exit") ) {
                exec.shutdown = true;
                break;
            }
        }
        
        System.out.println("MAIN THREAD EXITING");;
    }
    
}
