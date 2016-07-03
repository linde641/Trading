/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trading;

/**
 *
 * @author aronlindell
 */
public enum Action {
    
    BUY("BUY"),
    SELL("SELL"),
    SSHORT("SSHORT");
    
    private final String action;
    
    private Action(final String action)
    {
        this.action = action;
    }
    
    public String get()
    {
        return action;
    }
    
    public String getOpposite()
    {
        if (action.equals(BUY.action)){
            return SELL.action;
        }
        else if (action.equals(SELL.action)){
            return BUY.action;
        }
        else if (action.equals(SSHORT.action)){
            return BUY.action;
        }
        else 
            return "";
    }
}
