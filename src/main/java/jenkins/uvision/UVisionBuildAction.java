/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jenkins.uvision;

import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.logging.Level;
import static jenkins.uvision.UVisionPublisher.getCoverageXmlReport;
import org.kohsuke.stapler.StaplerProxy;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.localizer.Localizable;

/**
 *
 * @author prasad
 */
public class UVisionBuildAction implements HealthReportingAction, StaplerProxy {

    static final String ICON = "/plugin/uvision/clover_48x48.png";

    private transient WeakReference<TestRun> report;

    public AbstractBuild owner;
    private String buildBaseDir;
    private CoverageTarget healthyTarget;
    private CoverageTarget unhealthyTarget;

    private static final Logger logger = Logger.getLogger(UVisionBuildAction.class.getName());

    UVisionBuildAction(AbstractBuild owner, String workspacePath, TestRun r, CoverageTarget healthyTarget,
            CoverageTarget unhealthyTarget) {
        this.owner = owner;
        this.report = new WeakReference<TestRun>(r);
        this.buildBaseDir = workspacePath;
        if (this.buildBaseDir == null) {
            this.buildBaseDir = File.separator;
        } else if (!this.buildBaseDir.endsWith(File.separator)) {
            this.buildBaseDir += File.separator;
        }
        this.healthyTarget = healthyTarget;
        this.unhealthyTarget = unhealthyTarget;
        r.setOwner(owner);
    }

    public static UVisionBuildAction load(AbstractBuild<?, ?> build, String workspacePath, TestRun result, CoverageTarget healthyTarget,
            CoverageTarget unhealthyTarget) {
        return new UVisionBuildAction(build, workspacePath, result, healthyTarget, unhealthyTarget);
    }

    public HealthReport getBuildHealth() {
        if (healthyTarget == null || unhealthyTarget == null)
        {
            return null;
        }
        TestRun projectCoverage = getResult();
        Integer score = 100;
        score = healthyTarget.getRangeScores(unhealthyTarget, projectCoverage);
        
        return new HealthReport(score, "Code Coverage is " + healthyTarget.getAverageCoverage(projectCoverage) + "%");
    }

    public String getIconFileName() {
        return UVisionBuildAction.ICON;
    }

    public String getDisplayName() {
        return "Coverage Report";
    }

    public String getUrlName() {
        return "uvision";
    }

    public Object getTarget() {
        return getResult();
    }

    public synchronized TestRun getResult() {
        if (report != null) {
            TestRun r = report.get();
            if (r != null) {
                return r;
            }
        }

        File reportFile = UVisionPublisher.getCoverageXmlReport(owner);
        try {

            TestRun r = CoverageParser.parse(reportFile);
            r = healthyTarget.setMargin(r);
            r.setOwner(owner);

            report = new WeakReference<TestRun>(r);
            return r;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load " + reportFile, e);
            return null;
        }
    }

}
