package hudson.plugins.redmine;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.plugins.redmine.browser.BrowserLinks;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;
import hudson.scm.SubversionRepositoryBrowser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * produces redmine links.
 * 
 * @author gaooh
 * @date 2008/10/26
 */
public class RedmineRepositoryBrowser extends SubversionRepositoryBrowser {

    @DataBoundConstructor
    public RedmineRepositoryBrowser() {
    }

    @Override
    public URL getDiffLink(Path path) throws IOException {
        BrowserLinks links = BrowserLinks.createBrowserLinks(path.getLogEntry());
        return links.getDiffLink(path);
    }

    @Override
    public URL getFileLink(Path path) throws IOException {
        BrowserLinks links = BrowserLinks.createBrowserLinks(path.getLogEntry());
        return links.getFileLink(path);
    }

    @Override
    public URL getChangeSetLink(LogEntry changeSet) throws IOException {
        BrowserLinks links = BrowserLinks.createBrowserLinks(changeSet);
        return links.getChangeSetLink();
    }

    @Override
    public Descriptor<RepositoryBrowser<?>> getDescriptor() {
        return DESCRIPTOR;
    }

    private URL getRedmineURL(LogEntry logEntry) throws MalformedURLException {
        AbstractProject<?, ?> p = (AbstractProject<?, ?>) logEntry.getParent().build.getProject();
        RedmineProjectProperty rpp = p.getProperty(RedmineProjectProperty.class);
        if (rpp == null) {
            return null;
        } else {
            return new URL(rpp.redmineWebsite);
        }
    }

    private String getProject(LogEntry logEntry) {
        AbstractProject<?, ?> p = (AbstractProject<?, ?>) logEntry.getParent().build.getProject();
        RedmineProjectProperty rpp = p.getProperty(RedmineProjectProperty.class);
        if (rpp == null) {
            return null;
        } else {
            return rpp.projectName;
        }
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {

        public DescriptorImpl() {
            super(RedmineRepositoryBrowser.class);
        }

        public String getDisplayName() {
            return "Redmine";
        }
    }
}
