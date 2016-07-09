/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.EClientSocket;

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
    
    // com.ib.client class fields
    public Position position;
    public Contract contract;  
    public Order    order;
    OrderState      orderState;
    
    String          orderStatus;
    
    
    public Trade(String ticker, boolean active, double entry, double target,
            double stop, double metricRank, Contract contract, Position position, 
            Order order, OrderState orderState, int quantity, int clientId)
    {        
        this.ticker = ticker;        
        this.entry = entry;
        this.target = target;
        this.stop = stop;
        this.metricRank = metricRank;
        this.isLong = target > entry;
        this.isShort = target < entry;
        this.isActive = active;         
                
        this.position = position;
        this.contract = contract;
        this.order = order;
        this.orderState = orderState;
        this.quantity = quantity;        
        this.orderStatus = OrderStatus.NotSet.get();                        
    }        

    public Contract contract() { return contract; } 
    
    public void contract(Contract c) { this.contract = c; }    
    
    public void updateQuantity(int quantity) { this.quantity = quantity; }
    
    public int quantity() { return this.quantity; }    
    
    public Order order() { return this.order; }    
    
    public void order(Order o) { this.order = o; }
    
    public void initOrder(int clientId, int quantity)
    {                
        String action = "";
        
        Contract contract = contract();
        
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
        
        else if (isActive && isLong){ // exiting
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
            else if (contract.m_secType.equals("STK")){
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
        
        setMainOrderFields( int orderId, int clientId, int permId, String action, int totalQuantity,
            String orderType, double lmtPrice, double auxPrice)
        */
        setMainOrderFields(0, clientId, 0, action, quantity, "LMT", 0, 0);        
    }

    private void setMainOrderFields(int orderId, int clientId, int permId, String action, int totalQuantity,
            String orderType, double lmtPrice, double auxPrice){        
        order.m_orderId = orderId;
        order.m_clientId = clientId;
        order.m_permId = permId;
        order.m_action = action;
        order.m_totalQuantity = totalQuantity;
        order.m_orderType = orderType;
        order.m_lmtPrice = lmtPrice;
        order.m_auxPrice = auxPrice;
    }    
    
    public void updatePrice(double newPrice, int field)
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
    
    public double getPrice() { return last; }    
    
    public boolean enterTrade(EClientSocket client)
    {
        /*
        Maybe here is where I loop trying to get the best price etc. 
        */
        
        if (isActive){
            System.err.println("Error: Attempted to enter an active trade");            
        }
        else {
            order.m_lmtPrice = getPrice();
            client.placeOrder(order.m_orderId, contract, order);
            System.out.println("Waiting for execution of entry: " + toString());
            while( !orderStatus.equals(OrderStatus.Filled.get())){
                // block until order is executed fully
            }
            isActive = true;
        }
                
        return isActive;
    }
    
    public boolean exitTrade(EClientSocket client)
    {
//        this needs to have some sophistication to get the best possible price:
//        probably needs to get spreads, enter several limit orders up to 3 max or 
//                something and if they all fail just send a market order to get out
        if (!isActive){
            System.err.println("Error: Attempted to exit an inactive trade");            
        }
        else {
            order.m_lmtPrice = getPrice();
            client.placeOrder(order.m_orderId, contract, order);
            System.out.println("Waiting for execution of exit: " + toString());
            while( !orderStatus.equals(OrderStatus.Filled.get())){
                // block until order is executed fully
            }            
            isActive = false;
        }
        
        return !isActive;
    }
    
    /* Also need to implement trailing stops somewhere in this file probably */
    
    @Override
    public String toString()
    {        
        return this.ticker + " Quantity: " + quantity + ", conID: " + contract.m_conId;
    }
}
