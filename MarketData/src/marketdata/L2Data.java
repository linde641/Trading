/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marketdata;

import java.sql.Timestamp;

/**
 *
 * @author aronlindell
 */
public class L2Data {
    
    int tickerID;
    int position;
    int operation;
    int side;
    double price;
    int size;
    String mm;
    Timestamp ts;    
    

    public L2Data(int tickerID, int position, int operation, int side, double price, int size, String mm, Timestamp ts)
    {
        this.tickerID = tickerID;
        this.position = position;
        this.operation = operation;
        this.side = side;
        this.price = price;
        this.size = size;
        this.mm = mm;
        this.ts = ts;
    }
    
    @Override
    public String toString()
    {
        String sPos = String.valueOf(position);
        String sOp = String.valueOf(operation);
        String sSide = String.valueOf(side);
        String sPrice = String.valueOf(price);
        String sSize = String.valueOf(size);
        String smm = String.valueOf(mm);
        String sms = ts.toString().substring(11);
        
        return String.join(",", sPos, sOp, sSide, sPrice, sSize, smm, sms);
                
    }
    
    
}
