package hudson.plugins.redmine;

import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
            LOGGER.info("[Redmine] failed to get project property.");
            return;
        }

        String site = Util.fixEmptyAndTrim(rpp.redmineWebsite);
        String projectName = Util.fixEmptyAndTrim(rpp.projectName);
        String apiKey = Util.fixEmptyAndTrim(rpp.getApiKey());
        String version = Util.fixEmptyAndTrim(rpp.getVersion());

        if (site == null || projectName == null || apiKey == null) {
            return;
        }
        if (!isSupportedVersion(version)) {
            return;
        }
        
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
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "[Redmine] Invalid url. {0} : {1}", new String[]{site, e.getMessage()});
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[Redmine] failed to connect {0} : {1}", new String[]{site, e.getMessage()});
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
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
