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
public class BrowserLinks081 extends BrowserLinks {

    protected BrowserLinks081(LogEntry entry, RedmineProjectProperty property) {
        super(entry, property);
    }
    
    @Override
    public URL getDiffLink(Path path) throws IOException {
        LogEntry entry = getLogEntry();

        URL baseUrl = getRedmineURL(entry);
        String projectName = getProjectName(entry);
        String filePath = path.getValue();
        int revision = entry.getRevision();

        return new URL(baseUrl, "repositories/diff/" + projectName + filePath + "?rev=" + revision);
    }

    @Override
    public URL getFileLink(Path path) throws IOException {
        LogEntry entry = getLogEntry();

        URL baseUrl = getRedmineURL(entry);
        String projectName = getProjectName(entry);
        String filePath = path.getValue();

        return new URL(baseUrl, "repositories/entry/" + projectName + filePath);
    }

    @Override
    public URL getChangeSetLink() throws IOException {
        LogEntry entry = getLogEntry();

        URL baseUrl = getRedmineURL(entry);
        String projectName = getProjectName(entry);

        return new URL(baseUrl, "repositories/revision/" + projectName + "/" + entry.getRevision());
    }
}
