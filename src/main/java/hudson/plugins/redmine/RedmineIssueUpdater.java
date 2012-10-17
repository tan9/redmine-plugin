package hudson.plugins.redmine;

import hudson.Extension;
import hudson.Launcher;
import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import jenkins.model.Jenkins;
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

        String rootUrl = Jenkins.getInstance().getRootUrl();

        AbstractProject<?, ?> p = (AbstractProject<?, ?>) build.getProject();
        RedmineProjectProperty rpp = p.getProperty(RedmineProjectProperty.class);

        RedmineRestAPI api = rpp.getRedmineRestAPI();
        if (!api.isJavaAPISupported()) {
            logger.println("[Redmine] Java API not supported.");
            return true;
        }

        Pattern pattern = rpp.getPattern();
        for (Entry entry : build.getChangeSet()) {
            String text = entry.getMsg();
            String rev = entry.getCommitId();

            UpdateNotesListener uListener = new UpdateNotesListener(rpp, rev, build);
            IssueMarkupProcessor processor = new IssueMarkupProcessor(pattern, uListener);
            processor.process(new MarkupText(text));
        }

        return true;
    }

    private static class UpdateNotesListener implements IssueMarkupProcessor.IssueIdListener {

        private RedmineProjectProperty rpp;

        private String rev;

        private AbstractBuild<?, ?> build;

        public UpdateNotesListener(RedmineProjectProperty rpp, String rev, AbstractBuild<?, ?> build) {
            this.rpp = rpp;
            this.rev = rev;
            this.build = build;
        }

        public void onFirstIssueIdDetected(SubText text, int id) {
            updateNotes(id, rev, build);
        }

        public void onRestIssueIdDetected(SubText text, int start, int end, int id) {
            updateNotes(id, rev, build);
        }

        private void updateNotes(int id, String rev, AbstractBuild<?, ?> build) {
            RedmineRestAPI api = rpp.getRedmineRestAPI();
            try {
                api.updateNotes(id, rev, build);
            } catch (RedminePluginException ex) {
                Logger.getLogger(RedmineIssueUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
