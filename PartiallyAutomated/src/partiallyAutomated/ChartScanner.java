/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package partiallyAutomated;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author paidforbyoptions
 */
public class ChartScanner {
    /*
    Object basically needs to scan chart data that should be passed to public 
    methods for resistances/supports and identify entries/exits
    */
    
    // data
    public Candle[] chart;
    public int length;
    public List<PriceCountPair> resistances;
    public List<PriceCountPair> supports;
    
    private List<Integer> maxima;
    private List<Integer> minima;
    
    
    
    // methods
    
    public ChartScanner()
    {
        int i = 0;
        while(chart[i] != null){
            i++;
        }
        
        length = i;
        
        this.maxima = new LinkedList<Integer>();
        this.minima = new LinkedList<Integer>();
        this.resistances = new ArrayList<PriceCountPair> ();
        this.supports = new ArrayList<PriceCountPair> ();
    }
    
    public void findResistance(float factor)
    {// go through maxima and look for a repeated value(s) within some fudgefactor
        /* factor is the percentage (as a decimal) that values must be within of 
        eachother to be in the same bin in the list of key value pairs*/        
        
        resistances.add(new PriceCountPair(chart[maxima.get(0)].high, 1));
        int index = 0;
        float range = factor*chart[maxima.get(0)].high;
        
        for (int i = 1; i < maxima.size(); i++){
            index = maxima.get(i);
            float price = chart[index].high;
            
            
        }
    }
    
    public void findSupport(float factor)
    {// go through minima and look for a repeated value(s) within some fudgefactor
        
    }
    
    private void findExtrema(float factor)
    {        
//        factor is the percentage of the total chart length. If a point is higher than 
//        the points that percent of the length in both directions, it is a max and vice versa
        int interval = (int) Math.ceil(factor*length);        
        int runningHighs = 0;
        int runningLows = 0;
        
        if (interval == 0){
            interval = 1;
        }
                
        int i;
        for (i = interval; i < length; i++){
            for (int j = i - interval; j < i + interval ; j++){                
                if (chart[i].high >= chart[j].high){
                    runningHighs++;
                }
                else {
                    runningLows++;
                }                
            }
        
            if (runningHighs == 2*interval + 1){// its a max
                maxima.add(i);
            }
            else if (runningLows == 2*interval + 1){// its a min
                minima.add(i);
            }            
        }
    }
}
