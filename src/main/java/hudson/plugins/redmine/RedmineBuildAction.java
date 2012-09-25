package hudson.plugins.redmine;

import hudson.model.Action;

public class RedmineBuildAction implements Action {

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return "Redmine";
    }

    public String getUrlName() {
        return "redmine";
    }
}
