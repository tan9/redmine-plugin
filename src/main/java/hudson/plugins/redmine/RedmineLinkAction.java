package hudson.plugins.redmine;

import hudson.model.Action;

/**
 * @author gaooh
 * @date 2008/10/26
 */
public class RedmineLinkAction implements Action {

    private final RedmineProjectProperty prop;

    public RedmineLinkAction(RedmineProjectProperty prop) {
        this.prop = prop;
    }

    public String getIconFileName() {
        if (getUrlName() == null) {
            return null;
        }
        return "/plugin/redmine/redmine_fluid_icon.png"; 
    }

    public String getDisplayName() {
        return "Redmine";
    }

    public String getUrlName() {
        if (prop.getRedmineWebsite() == null) {
            return null;
        }
        if (prop.getProjectName() == null) {
            return prop.getRedmineWebsite();
        }
        return prop.getRedmineWebsite() + "projects/" + prop.getProjectName();
    }
}
