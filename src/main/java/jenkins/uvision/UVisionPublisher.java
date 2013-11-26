package jenkins.uvision;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.File;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link UVisionPublisher} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class UVisionPublisher extends Recorder {

    private final String coverageReportDir;
    private final String coverageReportFileName;
   
    private CoverageTarget healthyTarget;
    private CoverageTarget unhealthyTarget;
    private CoverageTarget failingTarget;
    
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public UVisionPublisher(String coverageReportDir, String coverageReportFileName) {
        this.coverageReportDir = coverageReportDir;
        this.coverageReportFileName = coverageReportFileName;
        this.healthyTarget = new CoverageTarget();
        this.unhealthyTarget = new CoverageTarget();
        this.failingTarget = new CoverageTarget();
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getCoverageReportDir() {
        return coverageReportDir;
    }

    public String getCoverageReportFileName() {
        return coverageReportFileName == null || coverageReportFileName.trim().length() == 0 ? "coverage.xml" : coverageReportFileName;
    }



    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException {
        
        final File buildRootDir = build.getRootDir(); 
        final FilePath buildTarget = new FilePath(buildRootDir);
        final FilePath workspace = build.getWorkspace();
        
        FilePath coverageReportPath = workspace.child(coverageReportDir);
        try {
            listener.getLogger().println("Publishing uVision coverage report...");

            // search one deep for the report dir, if it doesn't exist.
            if (!coverageReportPath.exists()) {
                coverageReportPath = findOneDirDeep(workspace, coverageReportDir);
            }

            // if the build has failed, then there's not
            // much point in reporting an error
            final boolean buildFailure = build.getResult().isWorseOrEqualTo(Result.FAILURE);
            final boolean missingReport = !coverageReportPath.exists();

            if (buildFailure && missingReport) {
                listener.getLogger().println("No Coverage report will be published due to a " + (buildFailure ? "Build Failure" : "missing report"));
                return true;
            }

            final boolean xmlExists = copyXmlReport(coverageReportPath, buildTarget, listener);

            processCoverageXml(build, listener, coverageReportPath, buildTarget);

        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("Unable to copy coverage from " + coverageReportPath + " to " + buildTarget));
            build.setResult(Result.FAILURE);
        }
        
        return true;
    }

    private boolean copyXmlReport(FilePath coverageReport, FilePath buildTarget, BuildListener listener) throws IOException, InterruptedException {
        // check one directory deep for a coverage.xml, if there is not one in the coverageReport dir already
       
        final FilePath coverageXmlPath = findOneDirDeep(coverageReport, getCoverageReportFileName());
        final FilePath toFile = buildTarget.child("coverage.xml");
        if (coverageXmlPath.exists()) {
            listener.getLogger().println("Publishing uVision coverage XML report...");
            coverageXmlPath.copyTo(toFile);
            return true;
        } else {
            listener.getLogger().println("Coverage xml file does not exist in: " + coverageReport +
                                         " called: " + getCoverageReportFileName() +
                                         " and will not be copied to: " + toFile);
            return false;
        }
    }
    
    private void processCoverageXml(AbstractBuild<?, ?> build, BuildListener listener, FilePath coverageReport, FilePath buildTarget) throws InterruptedException {
        String workspacePath = "";
        try {
            workspacePath = build.getWorkspace().act(new FilePath.FileCallable<String>() {
                public String invoke(File file, VirtualChannel virtualChannel) throws IOException {
                    try {
                        return file.getCanonicalPath();
                    } catch (IOException e) {
                        return file.getAbsolutePath();
                    }
                }
            });
        } catch (IOException e) {
        }
        if (!workspacePath.endsWith(File.separator)) {
            workspacePath += File.separator;
        }

        final File coverageXmlReport = getCoverageXmlReport(build);
        if (coverageXmlReport.exists()) {
            listener.getLogger().println("Publishing uVision coverage results...");
            TestRun result = null;
            try {
                result = CoverageParser.parse(coverageXmlReport);
            } catch (IOException e) {
                Util.displayIOException(e, listener);
                e.printStackTrace(listener.fatalError("Unable to copy coverage from " + coverageReport + " to " + buildTarget));
                build.setResult(Result.FAILURE);
            }
            
            final UVisionBuildAction action = UVisionBuildAction.load(build, workspacePath, result, healthyTarget, unhealthyTarget);

            build.getActions().add(action);
            Boolean failingMetrics = failingTarget.getFailingMetrics(result);
            if (failingMetrics) {
                listener.getLogger().println("Code coverage enforcement failed.");
                listener.getLogger().println("Setting Build to unstable.");
                build.setResult(Result.UNSTABLE);
            }

        } else {
            flagMissingCloverXml(listener, build);
        }
    }
    
    static File getCoverageXmlReport(AbstractBuild<?, ?> build) {
        return new File(build.getRootDir(), "coverage.xml");
    }
    
    private void flagMissingCloverXml(BuildListener listener, AbstractBuild<?, ?> build) {
        listener.getLogger().println("Could not find '" + coverageReportDir + "/" + getCoverageReportFileName() + "'.  Did you generate " +
                "the XML report for Clover?");
    }
    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public CoverageTarget getHealthyTarget() {
        return healthyTarget;
    }

    public void setHealthyTarget(CoverageTarget healthyTarget) {
        this.healthyTarget = healthyTarget;
    }

    public CoverageTarget getUnhealthyTarget() {
        return unhealthyTarget;
    }

    public void setUnhealthyTarget(CoverageTarget unhealthyTarget) {
        this.unhealthyTarget = unhealthyTarget;
    }

    public CoverageTarget getFailingTarget() {
        return failingTarget;
    }

    public void setFailingTarget(CoverageTarget failingTarget) {
        this.failingTarget = failingTarget;
    }

    

    /**
     * Descriptor for {@link UVisionPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/UVisionPublisher/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        
                /**
         * Creates a new instance of {@link CloverPublisher} from a submitted form.
         */
        @Override
        public UVisionPublisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            UVisionPublisher instance = req.bindParameters(UVisionPublisher.class, "coverage.");
            req.bindParameters(instance.failingTarget, "failingTarget.");
            req.bindParameters(instance.healthyTarget, "healthyTarget.");
            req.bindParameters(instance.unhealthyTarget, "unhealthyTarget.");
            
            if (instance.healthyTarget.getCoverage() == null) {
                instance.healthyTarget = new CoverageTarget(70);
            }
            
            return instance;
        }
        
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Publish uVision Coverage Report";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindParameters(this, "coverage.");
            save();
            return super.configure(req,formData);
        }
    }
    
        /**
     * Searches the current directory and its immediate children directories for filename.
     * The first occurence is returned.
     * @param startDir the dir to start searching in
     * @param filename the filename to search for
     * @return the path of filename
     * @throws IOException
     * @throws InterruptedException
     */
    private FilePath findOneDirDeep(final FilePath startDir, final String filename) throws IOException, InterruptedException {

        FilePath dirContainingFile = startDir;
        if (!dirContainingFile.child(filename).exists()) {
            // use the first directory with filename in it
            final List<FilePath> dirs = dirContainingFile.listDirectories();
            if (dirs != null) {
                for (FilePath dir : dirs) {
                    if (dir.child(filename).exists()) {
                        dirContainingFile = dir;
                        break;
                    }
                }
            }
        }
        return dirContainingFile.child(filename);
    }
}

