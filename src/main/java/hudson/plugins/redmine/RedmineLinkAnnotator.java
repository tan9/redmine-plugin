package hudson.plugins.redmine;

import hudson.Extension;
import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

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
        if (rpp == null || rpp.redmineWebsite == null) {
            return;
        }

        String url = rpp.redmineWebsite;
        for (LinkMarkup markup : MARKUPS) {
            markup.process(text, url);
        }
    }

    static final class LinkMarkup {

        private final Pattern pattern;

        private final String href;

        LinkMarkup(String pattern, String href) {
            pattern = NUM_PATTERN.matcher(pattern).replaceAll("([\\\\d|,| |&amp;|#]*#?\\\\d)");
            pattern = ANYWORD_PATTERN.matcher(pattern).replaceAll("((?:\\\\w|[._-])+)");
            this.pattern = Pattern.compile(pattern);
            this.href = href;
        }

        void process(MarkupText text, String url) {
            for (SubText st : text.findTokens(pattern)) {
                String[] message = st.getText().split(" ", 2);
                if (message.length <= 1) {
                    st.surroundWith("<a href='" + url + href + "'>", "</a>");
                    continue;
                }

                String[] nums = message[1].split(",|&amp;| ");
                if (nums.length <= 1) {
                    st.surroundWith("<a href='" + url + href + "'>", "</a>");
                    continue;
                }

                String splitValue = getSplitter(message[1]);

                int startpos = 0;
                int endpos = message[0].length() + 1;
                for (int i = 0; i < nums.length; i++) {
                    endpos += nums[i].length();
                    if (i > 0) {
                        endpos += splitValue.length();
                    }
                    endpos = Math.min(endpos, st.getText().length());
                    if (StringUtils.isNotBlank(nums[i])) {
                        nums[i] = nums[i].replace("#", "").trim();
                        st.addMarkup(startpos, endpos,
                                "<a href='" + url + "issues/" + nums[i] + "'>", "</a>");
                    }
                    startpos = endpos + splitValue.length();
                }
            }
        }
        
        private String getSplitter(String message) {
            String splitValue = ",";
            if (message.indexOf("&amp;") != -1) {
                splitValue = "&amp;";
            } else if (message.indexOf("#") != -1) {
                splitValue = "#";
            } else if (message.indexOf(" ") != -1) {
                splitValue = " ";
            }
            return splitValue;
        }

        private static final Pattern NUM_PATTERN = Pattern.compile("NUM");

        private static final Pattern ANYWORD_PATTERN = Pattern.compile("ANYWORD");
    }

    static final LinkMarkup[] MARKUPS = new LinkMarkup[]{
        new LinkMarkup(
        "(?:#|refs |references |IssueID |fixes |closes )#?NUM",
        "issues/$1"),
        new LinkMarkup(
        "((?:[A-Z][a-z]+){2,})|wiki:ANYWORD",
        "wiki/$1$2"),};
}
