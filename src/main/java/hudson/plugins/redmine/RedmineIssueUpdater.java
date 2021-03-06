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
import java.util.regex.Pattern;
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

        updateIssues(rpp, build);

        return true;
    }

    private void updateIssues(RedmineProjectProperty rpp, AbstractBuild<?, ?> build) {
        RedmineRestAPI api = rpp.getRedmineRestAPI();
        if (!api.isJavaAPISupported()) {
            return;
        }

        Pattern pattern = rpp.getPattern();

        for (Entry entry : build.getChangeSet()) {
            String text = entry.getMsg();
            String rev = entry.getCommitId();
            UpdateNotesListener listener = new UpdateNotesListener(api, build, rev);
            IssueMarkupProcessor processor = new IssueMarkupProcessor(pattern);
            processor.process(new MarkupText(text), listener);
        }
    }

    private static class UpdateNotesListener implements IssueMarkupProcessor.IssueIdListener {

        private RedmineRestAPI api;

        private String rev;

        private AbstractBuild<?, ?> build;

        public UpdateNotesListener(RedmineRestAPI api, AbstractBuild<?, ?> build, String rev) {
            this.api = api;
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
            try {
                api.updateNotes(id, rev, build);
            } catch (RedminePluginException ex) {
                //
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
