package hudson.plugins.mantis;

import hudson.MarkupText;
import hudson.Util;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.util.regex.Pattern;

/**
 * Creates HTML link for Mantis issues.
 * 
 * @author Seiji Sogabe
 */
public final class MantisLinkAnnotator extends ChangeLogAnnotator {

    private static final Pattern ISSUE_PATTERN =
            Pattern.compile("\\bissue #?(\\d+)\\b", Pattern.CASE_INSENSITIVE);

    @Override
    public void annotate(final AbstractBuild<?, ?> build, final Entry change,
            final MarkupText text) {
        final MantisProjectProperty mpp =
                build.getParent().getProperty(MantisProjectProperty.class);
        if (mpp == null || mpp.getSite() == null) {
            return;
        }

        final MantisBuildAction action = build.getAction(MantisBuildAction.class);

        final String url = mpp.getSite().getUrl().toExternalForm();
        for (final SubText st : text.findTokens(ISSUE_PATTERN)) {
            final Long id = Long.valueOf(st.group(1));
            final String newUrl = Util.encodeRFC2396(url + "view.php?id=$1");

            MantisIssue issue = null;
            if (action != null) {
                issue = action.getIssue(id);
            }

            if (issue == null) {
                st.surroundWith("<a href='" + newUrl + "'>", "</a>");
            } else {
                final String summary = Utility.escape(issue.getSummary());
                st.surroundWith(String.format("<a href='%s' tooltip='%s'>", newUrl,
                        summary), "</a>");
            }
        }
    }

}
