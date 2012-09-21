package hudson.plugins.redmine.browser;

import hudson.scm.EditType;
import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author sogabe
 */
public class BrowserLinks140 extends BrowserLinks {

    public BrowserLinks140(LogEntry entry) {
        super(entry);
    }
    
    @Override
    public URL getDiffLink(Path path) throws IOException {
        if (path.getEditType() != EditType.EDIT) {
            return null;
        }
        
        LogEntry entry = getLogEntry();
        
        URL baseUrl = getRedmineURL(entry);
        String projectName = getProject(entry);
        String filePath = path.getValue();
        int revision = entry.getRevision();
        
        return new URL(baseUrl, "projects/" + projectName + "/repository/diff" + filePath + "?rev=" + revision);
    }

    @Override
    public URL getFileLink(Path path) throws IOException {
        LogEntry entry = getLogEntry();

        URL baseUrl = getRedmineURL(entry);
        String projectName = getProject(entry);
        String filePath = path.getValue();
        int revision = path.getLogEntry().getRevision();

        return baseUrl == null ? null : new URL(baseUrl, "projects/" + projectName + "/repository/revisions/" + revision + "/entry" + filePath);
    }

    @Override
    public URL getChangeSetLink() throws IOException {
        LogEntry entry = getLogEntry();
        
        URL baseUrl = getRedmineURL(entry);
        String projectName = getProject(entry);
        
        return baseUrl == null ? null : new URL(baseUrl, "projects/" + projectName + "/repository/revisions/" + entry.getRevision());
    }

}
