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
    
    private final LogEntry logEntry;
    
    private final RedmineProjectProperty property;
    
    public static BrowserLinks createBrowserLinks(LogEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("LogEntry should not be null.");
        }
        
        AbstractProject<?, ?> p = (AbstractProject<?, ?>) entry.getParent().build.getProject();
        RedmineProjectProperty rpp = p.getProperty(RedmineProjectProperty.class);
        if (rpp == null) {
            throw new IllegalStateException("failed to get configuration.");
        }
        
        String ver = rpp.getVersion();
        if ("080".equals(ver)) {
            return new BrowserLinks080(entry, rpp);
        } else if ("081".equals(ver) || "090".equals(ver) || "130".equals(ver)) {
            return new BrowserLinks081(entry,rpp);
        }
        return new BrowserLinks140(entry, rpp);
    }

    protected BrowserLinks(LogEntry entry, RedmineProjectProperty property) {
        this.logEntry = entry;
        this.property = property;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }

    public RedmineProjectProperty getProperty() {
        return property;
    }
    
    public abstract URL getDiffLink(Path path) throws IOException;

    public abstract URL getFileLink(Path path) throws IOException;

    public abstract URL getChangeSetLink() throws IOException;

    protected URL getRedmineURL(LogEntry logEntry) throws MalformedURLException {
        return new URL(property.redmineWebsite);
    }

    protected String getProjectName(LogEntry logEntry) {
        return property.projectName;
    }
}
