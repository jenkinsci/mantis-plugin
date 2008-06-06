package hudson.plugins.mantis;

import hudson.model.Action;
import hudson.plugins.mantis.model.MantisIssue;

/**
 * Mantis issues related to the build.
 * 
 * @author Seiji Sogabe
 */
public final class MantisBuildAction implements Action {

    private static final long serialVersionUID = 1L;

    private final MantisIssue[] issues;

    public MantisIssue[] getIssues() {
        return issues;
    }

    public MantisBuildAction(final MantisIssue[] issues) {
        this.issues = issues;
    }

    public String getDisplayName() {
        return Messages.MantiBuildAction_Displayname();
    }

    public String getUrlName() {
        return "mantis";
    }

    public MantisIssue getIssue(final Long id) {
        for (final MantisIssue issue : issues) {
            if (issue.getId().equals(id)) {
                return issue;
            }
        }
        return null;
    }

    public String getIconFileName() {
        return null;
    }

}
