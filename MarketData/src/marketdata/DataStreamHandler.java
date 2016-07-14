/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marketdata;

/**
 *
 * @author paidforbyoptions
 */

import java.util.Date;
import java.sql.Timestamp;

import com.ib.client.EClientSocket;
import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;
import java.util.Calendar;




public class DataStreamHandler implements EWrapper{
    
    public Executive exec;
    public EClientSocket client;   
    public int nextOrderID;
    public int clientID;    
    public boolean msgComplete;

    
    public DataStreamHandler(Executive exec, int port, int clientID)
    {
        this.exec = exec;
        this.nextOrderID = 1; 
        this.clientID = clientID;        
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
        }                
    }
    
    @Override
    public void nextValidId(int orderId) {
        nextOrderID = orderId;
        System.out.println("nextOrderID received: " + orderId);
    }        

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        System.out.println("Called updateAccountValue: " + key + ", " + value + ", " + currency + ", " + accountName);                
    }

    @Override
    public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue,
            double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
        System.out.println("Called updatePortfolio: ");
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
        
        Timestamp ts = exec.getTimeStamp();
        
        String str = ts.toString().substring(11);
        System.out.println("tickPrice: " + tickerId + "," + field + "," + price + "ts: " + str);
        /*
        L2Data data = new L2Data(tickerId, 0, 0, 0, 0, 0, "", ts);
        exec.L2DataQueue.add(data);
        */
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
        //System.out.println("Called contractDetails");
        System.out.println("Valid Exchanges: " + contractDetails.m_validExchanges);
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
        
        //System.out.println("Called updateMktDepth");
        Timestamp ts = exec.getTimeStamp();                
        L2Data data = new L2Data(tickerId, position, operation, side, price, size, "", ts);
        exec.L2DataQueue.add(data);
    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker,
            int operation, int side, double price, int size) {
        
        //System.out.println("Called updateMktDepthL2");
        Timestamp ts = exec.getTimeStamp();                
        L2Data data = new L2Data(tickerId, position, operation, side, price, size, marketMaker, ts);
        exec.L2DataQueue.add(data);
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

