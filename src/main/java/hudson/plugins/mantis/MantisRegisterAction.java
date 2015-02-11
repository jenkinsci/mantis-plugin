package hudson.plugins.mantis;

import hudson.model.Action;

/**
 * Mantis Register Action 
 * 
 * @author Seiji Sogabe
 */
public class MantisRegisterAction implements Action {

    private MantisSite site;

    private int issueNo;

    public MantisRegisterAction(MantisSite site, int issueNo) {
        this.site = site;
        this.issueNo = issueNo;
    }

    public int getIssueNo() {
        return issueNo;
    }

    public MantisSite getSite() {
        return site;
    }

    public String getIssueLink() {
        return site.getIssueLink(issueNo);
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }
}
