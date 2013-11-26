/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jenkins.uvision;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author prasad
 */
public class CoverageTarget {

    private Integer coverage;

    public CoverageTarget() {
    }

    public CoverageTarget(Integer coverage) {
        this.coverage = coverage;
    }
    
    public Integer getCoverage() {
        return coverage;
    }

    public void setCoverage(Integer coverage) {
        this.coverage = coverage;
    }

    public Boolean getFailingMetrics(TestRun result) {
        return (getAverageCoverage(result) < coverage);
    }

    public Integer getAverageCoverage(TestRun result) {
        Integer total = 0;
        Integer count = 0;
        for (SuccessfulTests successfulTests : result.getSuccessfulTestses()) {
            for (Test test : successfulTests.getTests()) {
                total+=Integer.parseInt(test.getCoverage());
                count+=1;
            }
        }
        return total/count;
    }
    
    public Integer getRangeScores(CoverageTarget min, TestRun coverage) {
        Integer result = 0;
        result = calcRangeScore(getCoverage(), min.getCoverage(), getAverageCoverage(coverage));
        return result;
    }
    
    private static int calcRangeScore(Integer max, Integer min, int value) {
        if (min == null || min < 0) min = 0;
        if (max == null || max > 100) max = 100;
        if (min > max) min = max - 1;
        int result = (int)(100f * (value - min.floatValue()) / (max.floatValue() - min.floatValue()));
        if (result < 0) return 0;
        if (result > 100) return 100;
        return result;
    }
}
