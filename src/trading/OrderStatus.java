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

public enum OrderStatus {
    
    NotSet("NotSet"), // set trade.orderStatus set to this in constructor. Not a valid IB API value
    
    PendingSubmit("PendingSubmit"),
    PendingCancel("PendingCancel"),
    PreSubmitted("PreSubmitted"),
    Submitted("Submitted"),
    Cancelled("Cancelled"),
    Filled("Filled"),
    Inactive("Inactive");
    
    private final String status;
    
    private OrderStatus(final String status)
    {
        this.status = status;
    }
    
    public String get()
    {
        return status;
    }
}
