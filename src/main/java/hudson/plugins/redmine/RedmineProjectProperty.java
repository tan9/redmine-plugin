package hudson.plugins.redmine;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.ListBoxModel;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Property for {@link AbstractProject} that stores the associated Redmine website URL.
 * 
 * @author gaooh
 * @date 2008/10/13
 */
public class RedmineProjectProperty extends JobProperty<AbstractProject<?, ?>> {

    private static final String ISSUE_PATTERN 
            = "(?:#|refs |references |IssueID |fixes |closes )#?([\\d|,| |&amp;|#]*#?\\d)";
    
    private final String redmineWebsite;

    private final String projectName;

    private Boolean redmineVersion;
    
    private final String version;
    
    private final String apiKey;
    
    private final String accessKey;

    @DataBoundConstructor
    public RedmineProjectProperty(String redmineWebsite, String projectName, String version, String apiKey, String accessKey) {
        String site  = Util.fixEmptyAndTrim(redmineWebsite);
        if (site != null && !site.endsWith("/")) {
            this.redmineWebsite = site + '/';
        } else {
            this.redmineWebsite = site;
        }
        this.projectName = Util.fixEmptyAndTrim(projectName);
        this.version = Util.fixEmptyAndTrim(version);
        this.apiKey = Util.fixEmptyAndTrim(apiKey);
        this.accessKey = Util.fixEmptyAndTrim(accessKey);
    }

    public String getRedmineWebsite() {
        return redmineWebsite;
    }

    public String getProjectName() {
        return projectName;
    }
    
    public String getVersion() {
        return version;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public Pattern getPattern() {
        return Pattern.compile(ISSUE_PATTERN);
    }
    
    @Override
    public Action getJobAction(AbstractProject<?, ?> job) {
        return new RedmineLinkAction(this);
    }

    @Override
    public JobPropertyDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
    
    public RedmineRestAPI getRedmineRestAPI() {
        return new RedmineRestAPI(this);
    }
    
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends JobPropertyDescriptor {

        private transient String redmineWebsite;

        public DescriptorImpl() {
            super(RedmineProjectProperty.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        public String getDisplayName() {
            return "Associated Redmine website";
        }

        public ListBoxModel doFillVersionItems() {
            ListBoxModel model = new ListBoxModel();
            model.add("0.1.0 - 0.8.0", "080");
            model.add("0.8.1 - 0.8.7", "081");
            model.add("0.9.0 - 1.2.3", "090");
            model.add("1.3.0 - 1.3.3", "130");
            model.add("1.4.0 -      ", "140");
            return model;
        }
    }
    
    private static final Logger LOGGER = Logger.getLogger(RedmineProjectProperty.class.getName());    
}
