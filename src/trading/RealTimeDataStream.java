/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

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

public class RealTimeDataStream implements EWrapper{
    
    public EClientSocket client = null;   
    public int nextOrderID = 0;
    
    /**************************************************************************/
    /************************* Constructors ***********************************/
    /**************************************************************************/
    
    @SuppressWarnings({"empty-statement", "CallToPrintStackTrace"})
    public RealTimeDataStream(int port)
    {
        client = new EClientSocket(this);
        
        // connect to IB Gateway
        client.eConnect(null, port, 0);
        
        try
        {
            while(!client.isConnected()){
                System.out.println("not connected");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }       
    }
    
    

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
        try {
            System.out.println("tickPrice: " + tickerId + "," + field + "," + price);
            
            
        }
        catch (Error e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tickSize(int tickerId, int field, int size) {
        
    }

    @Override
    public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
        try {
            System.out.println("OptionPrice: " + tickerId + "," + field + "," + optPrice +  ", IV:" + impliedVol);
            
            
        }
        catch (Error e) {
            e.printStackTrace();
        }        
    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
        
    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {
        
    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {
        
    }

    @Override
    public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
        
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        
    }

    @Override
    public void openOrderEnd() {
        
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        
    }

    @Override
    public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
        
    }

    @Override
    public void updateAccountTime(String timeStamp) {
        
    }

    @Override
    public void accountDownloadEnd(String accountName) {
        
    }

    @Override
    public void nextValidId(int orderId) {
        nextOrderID = orderId;
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        System.out.println("got it");
    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        
    }

    @Override
    public void contractDetailsEnd(int reqId) {
        
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        
    }

    @Override
    public void execDetailsEnd(int reqId) {
        
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
        
    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {
        
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        
    }

    @Override
    public void managedAccounts(String accountsList) {
        
    }

    @Override
    public void receiveFA(int faDataType, String xml) {
        
    }

    @Override
    public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {	       
        try 
	{
		System.out.println("historicalData: " + reqId + "," + date + "," + 
                                    open + "," + high  + "," + low  + "," + close + "," +
                                    volume);
	} 
	catch (Exception e)
        {
		e.printStackTrace ();
        }        
    }

    @Override
    public void scannerParameters(String xml) {
        
    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
        
    }

    @Override
    public void scannerDataEnd(int reqId) {
        
    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
        
    }

    @Override
    public void currentTime(long time) {
        
    }

    @Override
    public void fundamentalData(int reqId, String data) {
        
    }

    @Override
    public void deltaNeutralValidation(int reqId, UnderComp underComp) {
        
    }

    @Override
    public void tickSnapshotEnd(int reqId) {
        
    }

    @Override
    public void marketDataType(int reqId, int marketDataType) {
        
    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {
        
    }

    @Override
    public void position(String account, Contract contract, int pos, double avgCost) {
        
    }

    @Override
    public void positionEnd() {
        
    }

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {
        
    }

    @Override
    public void accountSummaryEnd(int reqId) {
        
    }

    @Override
    public void verifyMessageAPI(String apiData) {
        
    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {
        
    }

    @Override
    public void displayGroupList(int reqId, String groups) {
        
    }

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {
        
    }

    @Override
    public void error(Exception e) {
        
    }

    @Override
    public void error(String str) {
        
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        
    }

    @Override
    public void connectionClosed() {
        
    }
    
}
