/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

import com.ib.client.Contract;

/**
 *
 * @author paidforbyoptions
 */

public class Trade {
    
    // data
    
    public String ticker;
    public boolean isActive;
    public boolean isLong;
    public boolean isShort;
    public float entry;
    public float target;
    public float stop; 
    public float metricRank; //could be risk/reward or something else eventually
    public float currentPrice;
    
    public Contract contract;
    
    /**************************************************************************/
    /************************* Constructors ***********************************/
    /**************************************************************************/
    
    public Trade(String ticker, boolean active, float entry, 
            float target, float stop, float metricRank, Contract contract)
    {
        this.ticker = ticker;        
        this.entry = entry;
        this.target = target;
        this.stop = stop;
        this.metricRank = metricRank;
        this.isLong = target > entry;
        this.isShort = target < entry;
        this.isActive = active; 
        this.currentPrice = updatePrice();
        
        this.contract = contract;
    }        
    
    /**************************************************************************/
    /************************* Methods ****************************************/
    /**************************************************************************/        
    
    public float updatePrice() // Calls IB API
    {
        return 0;
    }
    
    public boolean enterTrade()
    {
        if (isActive){
            System.err.println("Error: Attempted to enter an already active trade");
            return false;
        }
        if (isLong)
        {
            System.out.println("Entered: " + toString());
        }
        else if (isShort)
        {
            System.out.println("Entered: " + toString());
        }
        
        isActive = true;
        return isActive;
    }
    
    public void exitTrade()
    {
//        this needs to have some sophistication to get the best possible price:
//        probably needs to get spreads, enter several limit orders up to 3 max or 
//                something and if they all fail just send a market order to get out
    }
    
    /* Also need to implement trailing stops somewhere in this file probably */
    
    @Override
    public String toString()
    {
        return this.ticker + " entry: " + this.entry + " active: " + this.isActive;
    }
}
