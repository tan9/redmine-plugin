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
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Property for {@link AbstractProject} that stores the associated Redmine website URL.
 * 
 * @author gaooh
 * @date 2008/10/13
 */
public class RedmineProjectProperty extends JobProperty<AbstractProject<?, ?>> {

    public final String redmineWebsite;

    public final String projectName;

    private Boolean redmineVersion;
    
    private final String version;
    
    private final String apiKey;

    @DataBoundConstructor
    public RedmineProjectProperty(String redmineWebsite, String projectName, String version, String apiKey) {
        if (StringUtils.isBlank(redmineWebsite)) {
            redmineWebsite = null;
        } else {
            if (!redmineWebsite.endsWith("/")) {
                redmineWebsite += '/';
            }
        }
        this.redmineWebsite = redmineWebsite;
        this.projectName = projectName;
        this.version = version;
        this.apiKey = apiKey;
        
    }

    public String getVersion() {
        return version;
    }

    public String getApiKey() {
        return apiKey;
    }
    
    @Override
    public Action getJobAction(AbstractProject<?, ?> job) {
        return new RedmineLinkAction(this);
    }

    @Override
    public JobPropertyDescriptor getDescriptor() {
        return DESCRIPTOR;
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

        @Override
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            try {
                String redmineWebSite = Util.fixEmptyAndTrim(formData.getString("redmineWebsite"));
                String projectName = Util.fixEmptyAndTrim(formData.getString("projectName"));
                String version = Util.fixEmptyAndTrim(formData.getString("version"));
                String apiKey = Util.fixEmptyAndTrim(formData.getString("apiKey"));

                return new RedmineProjectProperty(redmineWebSite, projectName, version, apiKey);

            } catch (IllegalArgumentException e) {
                throw new FormException("redmine.redmineWebsite", "redmine.redmineWebSite");
            }
        }
        
        public ListBoxModel doFillVersionItems() {
            ListBoxModel model = new ListBoxModel();
            model.add("0.1.0 - 0.8.0", "080");
            model.add("0.8.1 - 0.8.7", "081");
            model.add("0.9.0 - 1.3.3", "090");
            model.add("1.4.0 -      ", "140");
            return model;
        }
    }
    
    private static final Logger LOGGER = Logger.getLogger(RedmineProjectProperty.class.getName());    
}
