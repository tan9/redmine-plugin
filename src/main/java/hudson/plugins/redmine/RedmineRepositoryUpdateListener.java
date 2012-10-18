package hudson.plugins.redmine;

import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Automatic refresh of repositories in Redmine on building
 *
 * @author Seiji Sogabe
 */
@Extension
public class RedmineRepositoryUpdateListener extends RunListener<Run> {

    @Override
    public void onStarted(Run r, TaskListener listener) {

        Job<?, ?> job = r.getParent();
        RedmineProjectProperty rpp = job.getProperty(RedmineProjectProperty.class);
        if (rpp == null) {
            return;
        }

        String site = rpp.getRedmineWebsite();
        String projectName = rpp.getProjectName();
        String apiKey = rpp.getApiKey();
        String version = rpp.getVersion();

        // required parameters
        if (site == null || projectName == null || apiKey == null) {
            return;
        }
        if (!isSupportedVersion(version)) {
            return;
        }

        fetchRedmineChangeSet(site, projectName, apiKey);
    }

    private void fetchRedmineChangeSet(String site, String projectName, String apiKey) {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL url = new URL(site + "sys/fetch_changesets?key=" + apiKey + "&id=" + projectName);
            conn = (HttpURLConnection) ProxyConfiguration.open(url);
            conn.setRequestMethod("GET");
            conn.connect();
            int code = conn.getResponseCode();
            if (code != 200) {
                LOGGER.log(Level.WARNING, "[Redmine] failed to connect {0} : response code {1}",
                        new String[]{site, String.valueOf(code)});
                return;
            }
            is = conn.getInputStream();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[Redmine] failed to fetch changeset. due to {0}", e.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // 
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    /**
     * Check whether automatic refresh is supported.
     */
    private boolean isSupportedVersion(String version) {
        if ("090".equals(version) || "130".equals(version) || "140".equals(version)) {
            return true;
        }
        return false;
    }

    private static final Logger LOGGER = Logger.getLogger(RedmineRepositoryUpdateListener.class.getName());
}
