package hudson.plugins.redmine;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.IOException;
import java.io.PrintStream;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class RedmineIssueUpdater extends Recorder {

    @DataBoundConstructor
    public RedmineIssueUpdater() {
    }
    
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) 
            throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        logger.println("[Redmine] Updaing Redmine...");
        
        AbstractProject<?, ?> p = (AbstractProject<?, ?>) build.getProject();
        RedmineProjectProperty rpp = p.getProperty(RedmineProjectProperty.class);
        
        // check version.Redmine Java API Library supports 1.3.0+ , 2.0.0+
        String version = rpp.getVersion();
        if (!isJavaAPISupported(version)) {
            logger.println("[Redmine] Java API not support this version.");
            return true;
        }
        
        return true;
    }
    
    private boolean isJavaAPISupported(String version) {
        if ("130".equals(version) || "140".equals(version)) {
            return true;
        }
        return false;
    }
    
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(RedmineIssueUpdater.class);
        }
        
        
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Redmineのチケットを更新";
        }
        
        
        @Override
        public Publisher newInstance(final StaplerRequest req, final JSONObject formData) {
            return req.bindJSON(RedmineIssueUpdater.class, formData);
        }
        
    }
}
