/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package partiallyAutomated;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author aronlindell
 */
public class Portfolio {
    
    String  acctCode;
    double  availableFunds;
    double  netLiquidation;
    double  maintMarginReq;
    double  unrealizedPnL;
    int     dayTradesRemaining;
    
    String updateTime;
    List<Position> positions;
    
    
    
    public Portfolio()
    {
        positions = new LinkedList();
    }
    
    public void updatePortfolio(String key, String value, String currency, String accountName)
    {
        if ( !acctCode.endsWith(accountName)) {
            System.out.println("ERROR, updatePortfolio: acct number mismatch");
            System.exit(-1); // handle better later
        }
        
        switch (key) {
            case "AvailableFunds":
                availableFunds = Double.parseDouble(value);
                break;
            case "NetLiquidation":
                netLiquidation = Double.parseDouble(value);
                break;
            case "MaintMarginReq":
                maintMarginReq = Double.parseDouble(value);
                break;
            case "UnrealizedPnL":
                unrealizedPnL = Double.parseDouble(value);
                break;
            case "DayTradesRemaining":
                dayTradesRemaining = Integer.parseInt(value);
                break;
            default: 
                System.out.println("Unhandled updateAccountValue: " + key);
        }                
    }
    
    public void updatePositions()
    {
        
    }
}
