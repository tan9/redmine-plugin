package hudson.plugins.redmine.browser;

import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author sogabe
 */
public class BrowserLinks080 extends BrowserLinks {

    public BrowserLinks080(LogEntry entry) {
        super(entry);
    }

    @Override
    public URL getDiffLink(Path path) throws IOException {
        LogEntry entry = getLogEntry();

        URL baseUrl = getRedmineURL(entry);
        String projectName = getProjectName(entry);
        String filePath = getFilePath(entry, path.getValue());
        int revision = entry.getRevision();

        return new URL(baseUrl, "repositories/diff/" + projectName + filePath + "?rev=" + revision);
    }

    @Override
    public URL getFileLink(Path path) throws IOException {
        LogEntry entry = getLogEntry();

        URL baseUrl = getRedmineURL(entry);
        String projectName = getProjectName(entry);
        String filePath = getFilePath(entry, path.getValue());

        return baseUrl == null ? null : new URL(baseUrl, "repositories/entry/" + projectName + filePath);
    }

    @Override
    public URL getChangeSetLink() throws IOException {
        LogEntry entry = getLogEntry();

        URL baseUrl = getRedmineURL(entry);
        String projectName = getProjectName(entry);

        return baseUrl == null ? null : new URL(baseUrl, "repositories/revision/" + projectName + "/" + entry.getRevision());
    }

    private String getFilePath(LogEntry logEntry, String fileFullPath) {
        String[] filePaths = fileFullPath.split("/");
        String filePath = "/";
        if (filePaths.length > 2) {
            for (int i = 2; i < filePaths.length; i++) {
                filePath = filePath + filePaths[i];
                if (i != filePaths.length - 1) {
                    filePath = filePath + "/";
                }
            }
        }
        
        return filePath;
    }
}
