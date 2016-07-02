/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ib.client;

/**
 *
 * @author aronlindell
 */
public enum Action {
    
    BUY("BUY"),
    SELL("SELL"),
    SSHORT("SSHORT");
    
    private String action;
    
    private Action(String action)
    {
        this.action = action;
    }
    
    public String get()
    {
        return action;
    }
}
