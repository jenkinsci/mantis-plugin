package hudson.plugins.mantis;

import hudson.model.Action;
import hudson.plugins.mantis.model.MantisIssue;
import java.util.regex.Pattern;

/**
 * Mantis issues related to the build.
 *
 * @author Seiji Sogabe
 */
public final class MantisBuildAction implements Action {

    private static final long serialVersionUID = 1L;

    private final MantisIssue[] issues;

    private final Pattern pattern;

    public MantisIssue[] getIssues() {
        return issues;
    }

    public MantisBuildAction(final Pattern pattern, final MantisIssue[] issues) {
        this.pattern = pattern;
        this.issues = issues;
    }

    public String getDisplayName() {
        return Messages.MantiBuildAction_Displayname();
    }

    public String getUrlName() {
        return "mantis";
    }

    public Pattern getPattern() {
        return pattern;
    }

    public MantisIssue getIssue(final int id) {
        for (final MantisIssue issue : issues) {
            if (issue.getId() == id) {
                return issue;
            }
        }
        return null;
    }

    public String getIconFileName() {
        return null;
    }

}
