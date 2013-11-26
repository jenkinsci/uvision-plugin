/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jenkins.uvision;

/**
 *
 * @author prasad
 */
public class Statistics {
    private String tests;
    private String failuresTotal;
    private String errors;
    private String ignored;
    private String failures;
    private String duration;

    public String getTests() {
        return tests;
    }

    public void setTests(String tests) {
        this.tests = tests;
    }

    public String getFailuresTotal() {
        return failuresTotal;
    }

    public void setFailuresTotal(String FailuresTotal) {
        this.failuresTotal = FailuresTotal;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String Errors) {
        this.errors = Errors;
    }

    public String getIgnored() {
        return ignored;
    }

    public void setIgnored(String ignored) {
        this.ignored = ignored;
    }

    public String getFailures() {
        return failures;
    }

    public void setFailures(String failures) {
        this.failures = failures;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tests : ").append(getTests()).append("\n");
        sb.append("FailuresTotal : ").append(getFailuresTotal()).append("\n");
        sb.append("Errors : ").append(getErrors()).append("\n");
        sb.append("Ignored : ").append(getIgnored()).append("\n");
        sb.append("Failures : ").append(getFailures()).append("\n");
        sb.append("Duration : ").append(getDuration()).append("\n");
        return sb.toString();
    }
    
    
}
