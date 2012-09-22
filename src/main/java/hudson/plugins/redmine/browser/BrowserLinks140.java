package hudson.plugins.redmine.browser;

import hudson.plugins.redmine.RedmineProjectProperty;
import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author sogabe
 */
public class BrowserLinks140 extends BrowserLinks {

    protected BrowserLinks140(LogEntry entry, RedmineProjectProperty property) {
        super(entry, property);
    }
    
    @Override
    public URL getDiffLink(Path path) throws IOException {
        LogEntry entry = getLogEntry();
        
        URL baseUrl = getRedmineURL(entry);
        String projectName = getProjectName(entry);
        String filePath = path.getValue();
        int revision = entry.getRevision();
        
        return new URL(baseUrl, "projects/" + projectName + "/repository/diff" + filePath + "?rev=" + revision);
    }

    @Override
    public URL getFileLink(Path path) throws IOException {
        LogEntry entry = getLogEntry();

        URL baseUrl = getRedmineURL(entry);
        String projectName = getProjectName(entry);
        String filePath = path.getValue();
        int revision = path.getLogEntry().getRevision();

        return baseUrl == null ? null : new URL(baseUrl, "projects/" + projectName + "/repository/revisions/" + revision + "/entry" + filePath);
    }

    @Override
    public URL getChangeSetLink() throws IOException {
        LogEntry entry = getLogEntry();
        
        URL baseUrl = getRedmineURL(entry);
        String projectName = getProjectName(entry);
        
        return baseUrl == null ? null : new URL(baseUrl, "projects/" + projectName + "/repository/revisions/" + entry.getRevision());
    }

}
