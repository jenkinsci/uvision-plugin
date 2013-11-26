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
public class SuccessfulTests {
    private Vector<Test> tests;
    
    public AbstractBuild owner = null;

    public SuccessfulTests() {
        tests = new Vector<Test>();
    }
    
    public void addTest(Test test)
    {
        tests.addElement(test);
    }

    public Vector<Test> getTests() {
        return tests;
    }

    public AbstractBuild getOwner() {
        return owner;
    }

    public void setOwner(AbstractBuild owner) {
        this.owner = owner;
    }    
    
}
