/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;


/**
 *
 * @author paidforbyoptions
 */

public class Trade {        
    
    public String ticker;
    public boolean isActive;
    public boolean isLong;
    public boolean isShort;
    public double entry;
    public double target;
    public double stop; 
    public double metricRank; //could be risk/reward or something else eventually
        
    public double bid;
    public double ask;
    public double last;
    public double high;
    public double low;
    public double close;
    
    public Contract contract;  
    public Order    order;
    OrderState      orderState;

    
    public Trade(String ticker, boolean active, double entry, double target,
            double stop, double metricRank, Contract contract, Order order,
            OrderState orderState)
    {
        this.ticker = ticker;        
        this.entry = entry;
        this.target = target;
        this.stop = stop;
        this.metricRank = metricRank;
        this.isLong = target > entry;
        this.isShort = target < entry;
        this.isActive = active;         
        
        this.contract = contract;        
        this.order = order;
        this.orderState = orderState;
    }        
    
     

    
    public void updatePrice(double newPrice, int field) // Calls IB API
    {
        //currentPrice = newPrice;
        switch (field) {
            case 1: // bid
                bid = newPrice;
                break;
            case 2:
                ask = newPrice;
                break;
            case 4:
                last = newPrice;
                break;
            case 6:
                high = newPrice;
                break;
            case 7:
                low = newPrice;
                break;
            case 9:
                close = newPrice;
                break;
        }        
    }
    
    public double getPrice()
    {
        return close;
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
        System.out.println("Exiting trade " + toString());
    }
    
    /* Also need to implement trailing stops somewhere in this file probably */
    
    @Override
    public String toString()
    {
        return this.ticker + " entry: " + this.entry + " active: " + this.isActive;
    }
}
