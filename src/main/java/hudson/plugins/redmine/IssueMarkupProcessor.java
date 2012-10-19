package hudson.plugins.redmine;

import hudson.MarkupText;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

public class IssueMarkupProcessor {

    private Pattern pattern;
    
    public IssueMarkupProcessor(Pattern pattern) {
        this.pattern = pattern;
    }

    public void process(MarkupText text,IssueIdListener listener) {
        for (MarkupText.SubText st : text.findTokens(pattern)) {
            String[] message = st.getText().split(" ", 2);
            if (message.length <= 1) {
                listener.onFirstIssueIdDetected(st, Integer.parseInt(st.group(1)));
                continue;
            }

            String[] nums = message[1].split(",|&amp;| ");
            if (nums.length <= 1) {
                listener.onFirstIssueIdDetected(st, Integer.parseInt(st.group(1)));
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
                    listener.onRestIssueIdDetected(st, startpos, endpos, Integer.parseInt(nums[i]));
                }
                startpos = endpos + splitValue.length();
            }
        }
    }

    public static interface IssueIdListener {

        void onFirstIssueIdDetected(MarkupText.SubText text, int id);

        void onRestIssueIdDetected(MarkupText.SubText text, int start, int end, int id);
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
