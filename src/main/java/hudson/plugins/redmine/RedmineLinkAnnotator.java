package hudson.plugins.redmine;

import hudson.Extension;
import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;
import java.util.regex.Pattern;

/**
 * Annotates <a href="http://www.redmine.org/wiki/redmine/RedmineSettings#Referencing-issues-in-commit-messages">RedmineLink</a>
 * notation in changelog messages.
 * 
 * @author gaooh
 * @date 2008/10/13
 */
@Extension
public class RedmineLinkAnnotator extends ChangeLogAnnotator {

    @Override
    public void annotate(AbstractBuild<?, ?> build, Entry change, MarkupText text) {
        RedmineProjectProperty rpp = build.getProject().getProperty(RedmineProjectProperty.class);
        if (rpp == null || rpp.getRedmineWebsite() == null) {
            return;
        }

        annotate(rpp, text);
    }

    private void annotate(RedmineProjectProperty rpp, MarkupText text) {
        Pattern pattern = rpp.getPattern();
        IssueIdAnnotateListener listener = new IssueIdAnnotateListener(rpp.getRedmineRestAPI(), rpp.getRedmineWebsite());
        IssueMarkupProcessor processor = new IssueMarkupProcessor(pattern, listener);
        processor.process(text);
    }

    private static class IssueIdAnnotateListener implements IssueMarkupProcessor.IssueIdListener {

        private RedmineRestAPI api;

        private String url;

        public IssueIdAnnotateListener(RedmineRestAPI api, String url) {
            this.api = api;
            this.url = url;
        }

        public void onFirstIssueIdDetected(SubText text, int id) {
            String subject = getSubject(api, id);
            if (subject == null) {
                text.surroundWith(String.format("<a href='%s%s/$1'>", url, "issues"), "</a>");
            } else {
                text.surroundWith(String.format("<a href='%s%s/$1' tooltip='%s'>", url, "issues", subject), "</a>");
            }
        }

        public void onRestIssueIdDetected(SubText text, int start, int end, int id) {
            String subject = getSubject(api, id);
            if (subject == null) {
                text.addMarkup(start, end,
                        String.format("<a href='%s%s/%s'>", url, "issues", id), "</a>");
            } else {
                text.addMarkup(start, end,
                        String.format("<a href='%s%s/%s' tooltip='%s'>", url, "issues", id, subject), "</a>");
            }
        }

        private String getSubject(RedmineRestAPI api, int id) {
            if (!api.isJavaAPISupported()) {
                return null;
            }
            String subject = null;
            try {
                subject = Utility.escape(api.getSubject(id));
            } catch (RedminePluginException e) {
                //
            }
            return subject;
        }
    }
}
