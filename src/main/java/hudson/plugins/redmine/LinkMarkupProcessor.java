package hudson.plugins.redmine;

import hudson.MarkupText;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

public class LinkMarkupProcessor {

    private static final Pattern NUM_PATTERN = Pattern.compile("NUM");

    public static final LinkMarkupProcessor[] MARKUPS = new LinkMarkupProcessor[]{
        new LinkMarkupProcessor(
        "(?:#|refs |references |IssueID |fixes |closes )#?NUM",
        "issues/$1")};

    private final Pattern pattern;

    private final String href;

    public LinkMarkupProcessor(String pattern, String href) {
        pattern = NUM_PATTERN.matcher(pattern).replaceAll("([\\\\d|,| |&amp;|#]*#?\\\\d)");
        this.pattern = Pattern.compile(pattern);
        this.href = href;
    }

    public List<RedmineIssue> process(MarkupText text, String url) {
        List<RedmineIssue> issues = new ArrayList<RedmineIssue>();
        
        for (MarkupText.SubText st : text.findTokens(pattern)) {
            String[] message = st.getText().split(" ", 2);
            if (message.length <= 1) {
                st.surroundWith("<a href='" + url + href + "'>", "</a>");
                issues.add(new RedmineIssue(Integer.parseInt(st.group(1))));
                continue;
            }

            String[] nums = message[1].split(",|&amp;| ");
            if (nums.length <= 1) {
                st.surroundWith("<a href='" + url + href + "'>", "</a>");
                issues.add(new RedmineIssue(Integer.parseInt(st.group(1))));
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
                    issues.add(new RedmineIssue(Integer.parseInt(nums[i])));
                }
                startpos = endpos + splitValue.length();
            }
        }
        return issues;
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
