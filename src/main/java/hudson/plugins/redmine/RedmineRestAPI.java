package hudson.plugins.redmine;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Issue;
import hudson.Util;
import hudson.model.AbstractBuild;
import jenkins.model.Jenkins;

public class RedmineRestAPI {

    private final RedmineProjectProperty rpp;
    
    private final RedmineManager mgr;

    RedmineRestAPI(RedmineProjectProperty rpp) {
        if (rpp == null) {
            throw new IllegalArgumentException("rpp should not be null.");
        }
        this.rpp = rpp;
        
        String url = rpp.getRedmineWebsite();
        String accessKey = rpp.getAccessKey();
        if (url == null || accessKey == null) {
            throw new IllegalArgumentException("url and accessKey should be not null.");
        }

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        this.mgr = new RedmineManager(url, accessKey);
    }

    public boolean isJavaAPISupported() {
        String version = rpp.getVersion();
        if ("130".equals(version) || "140".equals(version)) {
            return true;
        }
        
        return false;
    }
    
    public void updateNotes(int id, String rev, AbstractBuild<?, ?> build) throws RedminePluginException {
        ensureJavaAPISupported();
        try {
            Issue issue = mgr.getIssueById(id);
            issue.setNotes(createNotes(rev, build));
            mgr.update(issue);
        } catch (RedmineException e) {
            throw new RedminePluginException(e);
        }
    }

    public String getSubject(int id) throws RedminePluginException {
        ensureJavaAPISupported();
        Issue issue;
        try {
            issue = mgr.getIssueById(id);
        } catch (RedmineException ex) {
            throw new RedminePluginException(ex);
        }
        if (issue == null) {
            throw new RedminePluginException("No issue found.");
        }
        return issue.getSubject();
    }
    
    private void ensureJavaAPISupported() throws RedminePluginException {
        if (!isJavaAPISupported()) {
            throw new RedminePluginException("JAVA Rest API not supported.");
        }
    }
    
    private String createNotes(String rev, AbstractBuild<?, ?> build) {
        String rootUrl = Jenkins.getInstance().getRootUrl();
        String prjName = Util.escape(build.getProject().getName());
        int buildNo = build.getNumber();
        String buildUrl = rootUrl + build.getUrl();
        String jenkinsImg = rootUrl + "plugin/redmine/jenkins.png";
        String statusImg = rootUrl + "images/24x24/" + build.getResult().color.getImage();
        return "!" + jenkinsImg + "! リビジョン r" + rev + " を !" + statusImg + "! \"" + prjName + " #" + buildNo + "\":" + buildUrl + " に統合しました。";
    }
    
}
