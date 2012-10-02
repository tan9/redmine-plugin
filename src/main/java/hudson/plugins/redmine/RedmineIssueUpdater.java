package hudson.plugins.redmine;

import hudson.Extension;
import hudson.Launcher;
import hudson.MarkupText;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
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

        for (Entry entry : build.getChangeSet()) {
            String text = entry.getMsg();
            String rev = entry.getCommitId();
            for (RedmineIssue issue : getIssues(rpp, new MarkupText(text))) {
                int id = issue.getId();
                try {
                    api.updateNotes(id, rev, build);
                } catch (RedminePluginException e) {
                    logger.println("[Redmine] failed to update issue #" + id);
                    logger.println(e);
                }
            }
        }

        return true;
    }

    private List<RedmineIssue> getIssues(RedmineProjectProperty rpp, MarkupText text) {
        List<RedmineIssue> issues = new ArrayList<RedmineIssue>();
        Pattern pattern = rpp.getPattern();
        for (MarkupText.SubText st : text.findTokens(pattern)) {
            String[] message = st.getText().split(" ", 2);
            if (message.length <= 1) {
                issues.add(new RedmineIssue(Integer.parseInt(st.group(1))));
                continue;
            }

            String[] nums = message[1].split(",|&amp;| ");
            if (nums.length <= 1) {
                issues.add(new RedmineIssue(Integer.parseInt(st.group(1))));
                continue;
            }

            String splitValue = getSplitter(message[1]);

            int startpos = 0;
            int endpos = message[0].length() + 1;
            for (int i = 0; i < nums.length; i++) {
                endpos += nums[i].length();
                if (i > 0) {
                    endpos += splitValue.length();
                }
                endpos = Math.min(endpos, st.getText().length());
                if (StringUtils.isNotBlank(nums[i])) {
                    nums[i] = nums[i].replace("#", "").trim();
                    issues.add(new RedmineIssue(Integer.parseInt(nums[i])));
                }
                startpos = endpos + splitValue.length();
            }
        }
        return issues;
    }

    private String getSplitter(String message) {
        String splitValue = ",";
        if (message.indexOf("&amp;") != -1) {
            splitValue = "&amp;";
        } else if (message.indexOf("#") != -1) {
            splitValue = "#";
        } else if (message.indexOf(" ") != -1) {
            splitValue = " ";
        }
        return splitValue;
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
