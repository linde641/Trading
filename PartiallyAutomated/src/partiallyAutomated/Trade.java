/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package partiallyAutomated;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.EClientSocket;

/**
 *
 * @author paidforbyoptions
 */

public class Trade {        
    
    // fields from wathchList
    private String   ticker;
    private double   entry;
    private double   target;
    private double   stop;
    private int      quantity; // position
    private double   rank; //could be risk/reward or something else eventually
    private boolean  isActive;
    private String   secType;
    
    // fields inferred from watchlist
    private boolean  isLong;
    private boolean  isShort;                    
    
    // tickPrice fields
    private double   bid;
    private double   ask;
    private double   last;
    private double   high;
    private double   low;
    private double   close;
    
    // com.ib.client class fields
    private Position position;
    private Contract option;
    private Contract stock;
    private Order    order;
    private OrderState      orderState;    
    private String          orderStatus;
    
    
    public Trade(String ticker, double entry, double target,
            double stop, int quantity, double rank, boolean active, String secType, 
            Contract option, Contract stock, Position position, Order order, OrderState orderState)
    {        
        this.ticker = ticker;        
        this.entry = entry;
        this.target = target;
        this.stop = stop;
        this.rank = rank;
        this.isActive = active;
        this.secType = secType;
        
        this.isLong = target > entry;
        this.isShort = target < entry;        
                
        this.position = position;
        this.option = option;
        this.stock = stock;
        this.order = order;
        this.orderState = orderState;
        this.quantity = quantity;        
        this.orderStatus = OrderStatus.NotSet.get();                        
    }        

    public String   ticker() { return ticker; }
    public void     ticker(String ticker) { this.ticker = ticker; }   
    public double   entry() { return entry; }
    public void     entry(double entry) { this.entry = entry; }    
    public double   target() { return target; }
    public void     target(double target ) { this.target = target; }    
    public double   stop() { return stop; }
    public void     stop(double stop) { this.stop = stop; }    
    public int      quantity() { return quantity; }
    public void     quantity(int quantity) { this.quantity = quantity; }    
    public double   rank() { return rank; }
    public void     rank(double rank) { this.rank = rank; }    
    public boolean  isActive() { return isActive; }
    public void     isActive(boolean isActive) { this.isActive = isActive; }    
    public String   secType() { return secType; }
    public void     secType(String secType) { this.secType = secType; }    

//    public int      permID() { return permID; }
//    public void     permID(int permID) { this.permID = permID; }
    
    public boolean  isLong() { return isLong; }
    public void     isLong(boolean isLong) { this.isLong = isLong; }
    public boolean  isShort() { return isShort; }
    public void     isShort(boolean isShort) { this.isShort = isShort; }
        
    public Contract option() { return option; }     
    public void     option(Contract contract) { this.option = contract; }
    public Contract stock() { return stock; }
    public void     stock(Contract stock) { this.stock = stock; }
    public Contract contract()
    {
        if (secType.equals("OPT")) {
            return option();
        }
        else {
            return stock();
        }
    }    
    public Order    order() { return this.order; }        
    public void     order(Order o) { this.order = o; }
    public String   orderStatus() { return orderStatus; }
    public void     orderStatus(String status) { this.orderStatus = status; }
    
    public void initOrder(int clientId, int orderID, int initQuantity)
    {                
        String action = "";
        
        Contract contract = option();
        Contract stock = stock();
        
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
        setMainOrderFields(orderID, clientId, 0, action, initQuantity, "LMT", 0, 0);        
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
    
    public synchronized void updatePrice(double newPrice, int field)
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
    
    public synchronized double getPrice() { return last; }    
    
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
            client.placeOrder(order.m_orderId, option, order);
            System.out.println("Waiting for execution of entry: " + toString());
            /*
            while( !orderStatus.equals(OrderStatus.Filled.get())){
                // block until order is executed fully
            }
            */
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
            client.placeOrder(order.m_orderId, option, order);
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
        return this.ticker + " Quantity: " + quantity + ", conID: " + option.m_conId;
    }
}
