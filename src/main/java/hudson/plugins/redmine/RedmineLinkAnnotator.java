package hudson.plugins.redmine;

import hudson.Extension;
import hudson.MarkupText;
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
        if (rpp == null || rpp.getRedmineWebsite() == null) {
            return;
        }

        annotate(rpp, text);
    }

    private void annotate(RedmineProjectProperty rpp, MarkupText text) {
        Pattern pattern = rpp.getPattern();
        String url = rpp.getRedmineWebsite();

        RedmineRestAPI api = rpp.getRedmineRestAPI();

        for (MarkupText.SubText st : text.findTokens(pattern)) {
            String[] message = st.getText().split(" ", 2);
            if (message.length <= 1) {
                String subject = getSubject(api, st.group(1));
                if (subject == null) {
                    st.surroundWith(String.format("<a href='%s%s/$1'>", url, "issues"), "</a>");
                } else {
                    st.surroundWith(String.format("<a href='%s%s/$1' tooltip='%s'>", url, "issues", subject), "</a>");
                }
                continue;
            }

            String[] nums = message[1].split(",|&amp;| ");
            if (nums.length <= 1) {
                String subject = getSubject(api, st.group(1));
                if (subject == null) {
                    st.surroundWith(String.format("<a href='%s%s/$1'>", url, "issues"), "</a>");
                } else {
                    st.surroundWith(String.format("<a href='%s%s/$1' tooltip='%s'>", url, "issues", subject), "</a>");
                }
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
                    String subject = getSubject(api, nums[i]);
                    if (subject == null) {
                        st.addMarkup(startpos, endpos,
                                String.format("<a href='%s%s/%s'>", url, "issues", nums[i]), "</a>");                        
                    } else {
                        st.addMarkup(startpos, endpos,
                                String.format("<a href='%s%s/%s' tooltip='%s'>", url, "issues", nums[i], subject), "</a>");
                    }
                }
                startpos = endpos + splitValue.length();
            }
        }
    }

    private String getSubject(RedmineRestAPI api, String id) {
        if (!api.isJavaAPISupported()) {
            return null;
        }
        return Utility.escape(api.getSubject(id));
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
}
