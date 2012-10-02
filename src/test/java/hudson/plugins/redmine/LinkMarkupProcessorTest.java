package hudson.plugins.redmine;

import hudson.MarkupText;
import junit.framework.TestCase;

public class LinkMarkupProcessorTest extends TestCase {

    private static final String REDMINE_URL = "http://local.redmine/";

    public void testWikiLinkSyntax() {
        assertAnnotatedTextEquals("Nothing here.", "Nothing here.");
        assertAnnotatedTextEquals("#42", "<a href='" + REDMINE_URL + "issues/42'>#42</a>");
        assertAnnotatedTextEquals("IssueID 22", "<a href='" + REDMINE_URL + "issues/22'>IssueID 22</a>");
        assertAnnotatedTextEquals("fixes 10,11,12",
                "<a href='" + REDMINE_URL + "issues/10'>fixes 10</a>,"
                + "<a href='" + REDMINE_URL + "issues/11'>11</a>,"
                + "<a href='" + REDMINE_URL + "issues/12'>12</a>");
        assertAnnotatedTextEquals("references 110,111,112,113",
                "<a href='" + REDMINE_URL + "issues/110'>references 110</a>,"
                + "<a href='" + REDMINE_URL + "issues/111'>111</a>,"
                + "<a href='" + REDMINE_URL + "issues/112'>112</a>,"
                + "<a href='" + REDMINE_URL + "issues/113'>113</a>");
        assertAnnotatedTextEquals("closes 210, 211",
                "<a href='" + REDMINE_URL + "issues/210'>closes 210</a>, "
                + "<a href='" + REDMINE_URL + "issues/211'>211</a>");
        assertAnnotatedTextEquals("closes 210 211",
                "<a href='" + REDMINE_URL + "issues/210'>closes 210</a> "
                + "<a href='" + REDMINE_URL + "issues/211'>211</a>");
        assertAnnotatedTextEquals("refs 310, 11, 4, 4120",
                "<a href='" + REDMINE_URL + "issues/310'>refs 310</a>, "
                + "<a href='" + REDMINE_URL + "issues/11'>11</a>, "
                + "<a href='" + REDMINE_URL + "issues/4'>4</a>, "
                + "<a href='" + REDMINE_URL + "issues/4120'>4120</a>");
        assertAnnotatedTextEquals("refs 1&amp;11&amp;111&amp;1111",
                "<a href='" + REDMINE_URL + "issues/1'>refs 1</a>&amp;amp;"
                + "<a href='" + REDMINE_URL + "issues/11'>11</a>&amp;amp;"
                + "<a href='" + REDMINE_URL + "issues/111'>111</a>&amp;amp;"
                + "<a href='" + REDMINE_URL + "issues/1111'>1111</a>");
        assertAnnotatedTextEquals("IssueID 21&amp;11&amp;100",
                "<a href='" + REDMINE_URL + "issues/21'>IssueID 21</a>&amp;amp;"
                + "<a href='" + REDMINE_URL + "issues/11'>11</a>&amp;amp;"
                + "<a href='" + REDMINE_URL + "issues/100'>100</a>");
        assertAnnotatedTextEquals("refs #1,#11,#111,#1111",
                "<a href='" + REDMINE_URL + "issues/1'>refs #1</a>,"
                + "<a href='" + REDMINE_URL + "issues/11'>#11</a>,"
                + "<a href='" + REDMINE_URL + "issues/111'>#111</a>,"
                + "<a href='" + REDMINE_URL + "issues/1111'>#1111</a>");
        assertAnnotatedTextEquals("refs #1, #11, #111,#1111",
                "<a href='" + REDMINE_URL + "issues/1'>refs #1</a>, "
                + "<a href='" + REDMINE_URL + "issues/11'>#11</a>, "
                + "<a href='" + REDMINE_URL + "issues/111'>#111</a>,"
                + "<a href='" + REDMINE_URL + "issues/1111'>#1111</a>");
        assertAnnotatedTextEquals("refs #1",
                "<a href='" + REDMINE_URL + "issues/1'>refs #1</a>");
        assertAnnotatedTextEquals("closes #1&amp;#11",
                "<a href='" + REDMINE_URL + "issues/1'>closes #1</a>&amp;amp;"
                + "<a href='" + REDMINE_URL + "issues/11'>#11</a>");
        assertAnnotatedTextEquals("closes #1",
                "<a href='" + REDMINE_URL + "issues/1'>closes #1</a>");
        assertAnnotatedTextEquals("IssueID #1 #11",
                "<a href='" + REDMINE_URL + "issues/1'>IssueID #1</a> "
                + "<a href='" + REDMINE_URL + "issues/11'>#11</a>");
        assertAnnotatedTextEquals("#42 anything", "<a href='" + REDMINE_URL + "issues/42'>#42</a> anything");
        assertAnnotatedTextEquals("IssueID 22 is fixed", "<a href='" + REDMINE_URL + "issues/22'>IssueID 22</a> is fixed");
        assertAnnotatedTextEquals("fixes 10,11,12,#13 are fixed",
                "<a href='" + REDMINE_URL + "issues/10'>fixes 10</a>,"
                + "<a href='" + REDMINE_URL + "issues/11'>11</a>,"
                + "<a href='" + REDMINE_URL + "issues/12'>12</a>,"        
                + "<a href='" + REDMINE_URL + "issues/13'>#13</a> are fixed");        
        assertAnnotatedTextEquals("fixes 10 refs 11",
                "<a href='" + REDMINE_URL + "issues/10'>fixes 10</a> "
                + "<a href='" + REDMINE_URL + "issues/11'>refs 11</a>");        
    }

    private void assertAnnotatedTextEquals(String originalText, String expectedAnnotatedText) {
        MarkupText markupText = new MarkupText(originalText);
        for (LinkMarkupProcessor markup : LinkMarkupProcessor.MARKUPS) {
            markup.process(markupText, REDMINE_URL);
        }

        System.out.println(markupText.toString());
        assertEquals(expectedAnnotatedText, markupText.toString());
    }
    
}
