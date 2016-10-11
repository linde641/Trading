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
public class PriceCountPair {
    
    // data
    public float price;
    public int   count;
    
    // methods
    public PriceCountPair(float price, int count)
    {
        this.price = price;
        this.count = count;
    }
    
    public void incrementPairValue()
    {
        count++;
    }
}
