/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.Action;

/**
 *
 * @author paidforbyoptions
 */

public class Trade {        
    
    public String   ticker;
    public boolean  isActive;
    public boolean  isLong;
    public boolean  isShort;
    public double   entry;
    public double   target;
    public double   stop; 
    public double   metricRank; //could be risk/reward or something else eventually    
    public int      quantity; // position
    
    // tickPrice fields
    public double   bid;
    public double   ask;
    public double   last;
    public double   high;
    public double   low;
    public double   close;
    
    /*
    Contract contract, int position, double marketPrice, double marketValue,
            double averageCost, double unrealizedPNL, double realizedPNL, String accountName)
    */
    
    /*
    // updatePortfolio args
    
    public double   marketPrice;
    public double   marketValue;
    public double   averageCost;
    public double   unrealizedPNL;
    public double   realizedPNL;
    */
    
    public Contract contract;  
    public Order    order;
    OrderState      orderState;
    
    
    public Trade(String ticker, boolean active, double entry, double target,
            double stop, double metricRank, Contract contract, Order order,
            OrderState orderState, int quantity, int clientId)
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
        this.quantity = quantity;
        
        initOrder(clientId, quantity);
    }        

    
    private void initOrder(int clientId, int quantity)
    {                
        String action = "";
        
        if ( !isActive && isLong){
            if (contract.m_secType.equals("OPT")){
                switch (contract.m_right) {
                    case "CALL":
                        System.out.println("WARNING: Generating a CALL BUY TO OPEN order");
                        action = Action.BUY.get();
                        break;
                    case "PUT":
                        System.out.println("WARNING: Generating a PUT SELL TO OPEN order");
                        action = Action.SSHORT.get();                    
                        break;
                    default:
                        System.out.println("ERROR: initOrder : EXITING");
                        System.exit(-1);
                        break;
                }
            }
            else if (contract.m_secType.equals("STK")){
                System.out.println("WARNING: Generating a STK BUY TO OPEN order");
                action = Action.BUY.get();                    
            }
        }
        
        else if ( !isActive && isShort){
            if (contract.m_secType.equals("OPT")){
                switch (contract.m_right) {
                    case "PUT":
                        System.out.println("WARNING: Generating a PUT BUY TO OPEN order");
                        action = Action.BUY.get();                    
                        break;
                    case "CALL":
                        System.out.println("WARNING: Generating a CALL SELL TO OPEN order");
                        action = Action.SSHORT.get();                    
                        break;
                    default:
                        System.out.println("ERROR: initOrder : EXITING");
                        System.exit(-1);
                        break;
                }
            }
            else if (contract.m_secType.equals("STK")){
                System.out.println("WARNING: Generating a STK SELL TO OPEN order");
                action = Action.SSHORT.get();                    
            }
        }
        
        else if (isActive && isLong){
            if (contract.m_secType.equals("OPT")){
                switch (contract.m_right) {
                    case "CALL":
                        System.out.println("WARNING: Generating a CALL SELL TO COVER order");
                        action = Action.SELL.get();                    
                        break;
                    case "PUT":
                        System.out.println("WARNING: Generating a PUT BUY TO COVER order");
                        action = Action.BUY.get();                    
                        break;
                    default:
                        System.out.println("ERROR: initOrder : EXITING");
                        System.exit(-1);
                        break;
                }
            }
            else if (contract.m_secType.equals("OPT")){
                System.out.println("WARNING: Generating a STK SELL TO COVER order");
                action = Action.SELL.get();                    
            }
        }
        
        else if (isActive && isShort){
            if (contract.m_secType.equals("OPT")){
                switch (contract.m_right) {
                    case "PUT":
                        System.out.println("WARNING: Generating a PUT SELL TO COVER order");
                        action = Action.SELL.get();                    
                        break;                
                    case "CALL":
                        System.out.println("WARNING: Generating a CALL BUY TO COVER order");
                        action = Action.BUY.get();                    
                        break;
                    default:
                        System.out.println("ERROR: initOrder : EXITING");
                        System.exit(-1);
                        break;
                }
            }
            else if (contract.m_secType.equals("STK")){
                System.out.println("WARNING: Generating a STK BUY TO COVER order");
                action = Action.BUY.get();
            }
        }   
        
        /*
        Order ID of 0 is hardcoded in as first argument to constructor. It will be 
        set non-zero once the trade triggers.
        PermId also set to 0. Not exactly sure how this works yet 6/26/16
        same with LMT price and AUX price
        */
        
        order.setMainOrderFields(0, clientId, 0, action, quantity, "LMT", 0, 0);        
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
        return last;
    }
    
    public void updateQuantity(int quantity)
    {
        this.quantity = quantity;
    }
    
    public int getQuantity()
    {
        return this.quantity;
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
