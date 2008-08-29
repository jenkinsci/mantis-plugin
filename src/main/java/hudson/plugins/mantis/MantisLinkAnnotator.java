package hudson.plugins.mantis;

import hudson.MarkupText;
import hudson.Util;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Creates HTML link for Mantis issues.
 *
 * @author Seiji Sogabe
 */
public final class MantisLinkAnnotator extends ChangeLogAnnotator {

    @Override
    public void annotate(final AbstractBuild<?, ?> build, final Entry change,
            final MarkupText text) {
        final MantisProjectProperty mpp =
                build.getParent().getProperty(MantisProjectProperty.class);
        if (mpp == null || mpp.getSite() == null) {
            return;
        }
        if (!mpp.isLinkEnabled()) {
            return;
        }

        final MantisBuildAction action = build.getAction(MantisBuildAction.class);

        final String url = mpp.getSite().getUrl().toExternalForm();
        final Pattern pattern = mpp.getRegExp();
        for (final SubText st : text.findTokens(pattern)) {
            final int id = Integer.valueOf(st.group(1));
            final String newUrl = Util.encodeRFC2396(url + "view.php?id=$1");

            MantisIssue issue = null;
            if (action != null) {
                issue = action.getIssue(id);
            }

            if (issue == null) {
                LOGGER.log(Level.WARNING, Messages.MantisLinkAnnotator_FailedToGetMantisIssue(id));
                st.surroundWith("<a href='" + newUrl + "'>", "</a>");
            } else {
                final String summary = Utility.escape(issue.getSummary());
                st.surroundWith(String.format("<a href='%s' tooltip='%s'>", newUrl,
                        summary), "</a>");
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(MantisLinkAnnotator.class.getName());
}
