package hudson.plugins.redmine;

public class RedminePluginException extends Exception {

    public RedminePluginException() {
    }

    public RedminePluginException(String message) {
        super(message);
    }

    public RedminePluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedminePluginException(Throwable cause) {
        super(cause);
    }
    
}
