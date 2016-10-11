/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package partiallyAutomated;

/**
 *
 * @author paidforbyoptions
 */

import com.ib.client.EClientSocket;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;
import java.util.Map;


public class DataStreamHandler implements EWrapper {
    
    public Executive exec;
    public EClientSocket client;   
    public int nextOrderID;
    public int clientID;    
    public boolean msgComplete;
        
    public final Object orderIDLock;
    
    public DataStreamHandler(Executive exec, int port, int clientID)
    {
        this.exec = exec;
        this.nextOrderID = 1; 
        this.clientID = clientID;
        this.orderIDLock = new Object();
        client = new EClientSocket(this);
        
        // connect to IB Gateway
        client.eConnect(null, port, clientID);
        
        if (!client.isConnected()){
            System.out.println("Not Connected");
        }
        
        msgComplete = true;
    }

    
    
    @Override
    public void managedAccounts(String accountsList) {
        System.out.println("Managed Accounts: ");
        String[] accounts = accountsList.split(",");
        
        for (String acct : accounts) {
            System.out.println(acct);
            Portfolio p = new Portfolio();
            p.acctCode = acct;
            exec.portfolios.put(acct, p); // finishing off exec constructor               
        }
        
        exec.flag = true;
        //System.out.println("End Managed Accounts");
    }
    
    @Override
    public void nextValidId(int orderId) {
        synchronized(orderIDLock) {
            nextOrderID = orderId;
        }
        System.out.println("nextOrderID received: " + orderId);
        if (orderId != 1) {
            System.out.println("NEXT VALID ID RECEIVED NOT EQUAL TO 1: ORDER ID IS STILL INDEX INTO ORDER LIST: EXITING");
            System.exit(-1);
        }
    }        

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        System.out.println("Called updateAccountValue: " + key + ", " + value + ", " + currency + ", " + accountName);        
        
        exec.portfolios.get(accountName).updatePortfolio(key, value, currency, accountName);
    }

    @Override
    public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue,
            double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
        System.out.println("Called updatePortfolio: ");
        
        Position newPosition = new Position(contract, accountName, position, 
            marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL);

        exec.portfolios.get(accountName).positions.add(newPosition);
        
        /*        
        for (Trade trade : exec.watchList) {
            if (trade.contract.m_conId == contract.m_conId) {
                trade.contract = contract;
                if (trade.quantity != position) {
                    System.out.println("WARNING, updatePortfolio: position mismatch with watchList. Overwriting");
                    trade.quantity = position;
                }
                
                
            }
        }
        */
    }

    @Override
    public void updateAccountTime(String timeStamp) {
        System.out.println("Called updateAccountTime");        
    }

    @Override
    public void accountDownloadEnd(String accountName) {
        System.out.println("Called accountDownloadEnd");      
        
    }    
    
    @Override    
    public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
        System.out.println("tickPrice: " + tickerId + "," + field + "," + price);   
        exec.watchList.get(tickerId).updatePrice(price, field);            
    }

    @Override
    public void tickSize(int tickerId, int field, int size) {
        System.out.println("Called tickSize");
    }

    @Override
    public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta,
            double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {        
        System.out.println("OptionPrice: " + tickerId + "," + field + "," + optPrice +  ", IV:" + impliedVol);
    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
        System.out.println("Called tickGeneric");
    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {
        System.out.println("Called tickString");
    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints,
            double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {
        System.out.println("Called tickEFP");
    }

    @Override
    public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice,
            int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
        // called when the status of an order changes        
        System.out.println("Called orderStatus");
        /*
        if ( ! exec.orderIDtoPermID.containsKey(orderId)) { // this is first orderStatus for that order
            exec.orderIDtoPermID.put(orderId, permId);            
        }
        else {   // order ID key already exists in map; at least make sure the permID agrees
            if (exec.orderIDtoPermID.get(orderId) != permId) {
                System.out.println("Received permId: " + permId + " for " + orderId + " that doesn't match existing permId");
                System.exit(-1);
            }
        }
        */   
        Map<Integer, Integer> permToTicker = exec.permToTicker;
        Map<Integer, Integer> tickerToOrder = exec.tickerToOrder;
        
        if (permToTicker.containsKey(permId)) { // must be a startup order status message
            int tickerID = exec.permToTicker.get(permId); // this should be populated from import file at startup
            exec.tickerToOrder.put(tickerID, orderId);
            Trade trade = exec.watchList.get(tickerID);
            trade.orderStatus(status);
        }
        else { // an order status in response to an order that was entered during this session
            for (Integer key : tickerToOrder.values()) {
                if (tickerToOrder.get(key) == orderId) {
                    permToTicker.put(key, permId);
                }
            }
        }
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        System.out.println("Called openOrder");
        
    }

    @Override
    public void openOrderEnd() {
        System.out.println("Called openOrderEnd");
    }
    
    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        // called when an order is executed
        System.out.println("Called execDetails");                
    }

    @Override
    public void execDetailsEnd(int reqId) {
        System.out.println("Called execDetailsEnd");
    }    

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        System.out.println("Called contractDetails");
    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        System.out.println("Called bondContractDetails");
    }

    @Override
    public void contractDetailsEnd(int reqId) {
        System.out.println("Called contractDetailsEnd");
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
        System.out.println("Called updateMktDepth");
    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker,
            int operation, int side, double price, int size) {
        System.out.println("Called updateMktDepthL2");
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        System.out.println("Called updateNewsBulletin");
    }

    @Override
    public void receiveFA(int faDataType, String xml) {
        System.out.println("Called receiveFA");
    }

    @Override
    public void historicalData(int reqId, String date, double open, double high, double low,
            double close, int volume, int count, double WAP, boolean hasGaps) {	       
        System.out.println("Called historicalData");
        System.out.println("historicalData: " + reqId + "," + date + "," + 
            open + "," + high  + "," + low  + "," + close + "," + volume);	
    }

    @Override
    public void scannerParameters(String xml) {
        System.out.println("Called scannerParameters");
    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails,
            String distance, String benchmark, String projection, String legsStr) {
        System.out.println("Called scannerData");
    }

    @Override
    public void scannerDataEnd(int reqId) {
        System.out.println("Called scannerDataEnd");
    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low,
            double close, long volume, double wap, int count) {
        System.out.println("Called realtimeBar");
    }

    @Override
    public void currentTime(long time) {
        System.out.println("Called currentTime");
    }

    @Override
    public void fundamentalData(int reqId, String data) {
        System.out.println("Called fundamentalData");
    }

    @Override
    public void deltaNeutralValidation(int reqId, UnderComp underComp) {
        System.out.println("Called deltaNeutralValidation");
    }

    @Override
    public void tickSnapshotEnd(int reqId) {
        System.out.println("Called tickSnapshotEnd");
    }

    @Override
    public void marketDataType(int reqId, int marketDataType) {
        System.out.println("Called marketDataType");
    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {
        System.out.println("Called comissionReport");
    }

    @Override
    public void position(String account, Contract contract, int pos, double avgCost) {
        System.out.println("Called position");
    }

    @Override
    public void positionEnd() {
        System.out.println("Called positionEnd");
    }

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {
        System.out.println("Called accountSummary");
    }

    @Override
    public void accountSummaryEnd(int reqId) {
        System.out.println("Called accountSummaryEnd");
    }

    @Override
    public void verifyMessageAPI(String apiData) {
        System.out.println("Called verifyMessageAPI");
    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {
        System.out.println("Called verifyCompleted");
    }

    @Override
    public void displayGroupList(int reqId, String groups) {
        System.out.println("Called displayGroupList");
    }

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {
        System.out.println("Called displayGroupUpdated");
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void error(Exception e) {        
        e.printStackTrace();
    }

    @Override
    public void error(String str) {
        System.out.println(str);
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {        
        System.out.println("Error Code: " + errorCode + ", Error Msg: " + errorMsg + " ID: " + id);        
    }

    @Override
    public void connectionClosed() {
        System.out.println("Called connectionClosed");
    }
    
}
