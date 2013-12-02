/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jenkins.uvision;

import hudson.model.AbstractBuild;
import java.util.Vector;

/**
 *
 * @author prasad
 */
public class TestRun {
    private Vector<SuccessfulTests> successfulTestses;
    private Vector<Statistics> statisticses;
    private Statistics statistics;
    public AbstractBuild owner = null;
    
    public TestRun() {
        successfulTestses = new Vector<SuccessfulTests>();
        statisticses = new Vector<Statistics>();
    }     

    public void addSuccessfullTests(SuccessfulTests successfulTests)
    {
        successfulTestses.addElement(successfulTests);
    }
    
    public void addStatistics(Statistics statistics)
    {
        statisticses.addElement(statistics);
    }

    public Vector<SuccessfulTests> getSuccessfulTestses() {
        return successfulTestses;
    }

    public Vector<Statistics> getStatisticses() {
        if(statisticses.isEmpty()){
            statistics = new Statistics("N/A", "N/A", "N/A", "N/A", "N/A", "N/A");
            statisticses.add(statistics);
        }
        return statisticses;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public Statistics getStatistics() {        
        return statistics;
    }  

    public void setOwner(AbstractBuild owner) {
        this.owner = owner;        
        for(SuccessfulTests successfulTests : successfulTestses)
        {
            successfulTests.setOwner(owner);
        }
    }

    public AbstractBuild getOwner() {
        return owner;
    }
    
    
    
}
