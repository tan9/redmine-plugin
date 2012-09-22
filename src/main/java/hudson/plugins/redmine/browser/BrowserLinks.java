package hudson.plugins.redmine.browser;

import hudson.model.AbstractProject;
import hudson.plugins.redmine.RedmineProjectProperty;
import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author sogabe
 */
public abstract class BrowserLinks {
    
    private LogEntry logEntry;
    
    public static BrowserLinks createBrowserLinks(LogEntry entry) {
        AbstractProject<?, ?> p = (AbstractProject<?, ?>) entry.getParent().build.getProject();
        RedmineProjectProperty rpp = p.getProperty(RedmineProjectProperty.class);
        if (rpp == null) {
            throw new IllegalStateException("failed to get configuration.");
        }
        
        String ver = rpp.getVersion();
        if ("080".equals(ver)) {
            return new BrowserLinks080(entry);
        } else if ("081".equals(ver)) {
            return new BrowserLinks081(entry);
        }
        return new BrowserLinks140(entry);
    }

    public BrowserLinks(LogEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("LogEntry should not be null.");
        }
        this.logEntry = entry;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }
    
    public abstract URL getDiffLink(Path path) throws IOException;

    public abstract URL getFileLink(Path path) throws IOException;

    public abstract URL getChangeSetLink() throws IOException;

    protected RedmineProjectProperty getRedmineProjectProperty(LogEntry logEntry) {
        AbstractProject<?, ?> p = (AbstractProject<?, ?>) logEntry.getParent().build.getProject();
        return p.getProperty(RedmineProjectProperty.class);
    }

    protected URL getRedmineURL(LogEntry logEntry) throws MalformedURLException {
        RedmineProjectProperty rpp = getRedmineProjectProperty(logEntry);
        if (rpp == null) {
            return null;
        } 
        return new URL(rpp.redmineWebsite);
    }

    protected String getProjectName(LogEntry logEntry) {
        RedmineProjectProperty rpp = getRedmineProjectProperty(logEntry);
        if (rpp == null) {
            return null;
        } 
        return rpp.projectName;
    }
}
