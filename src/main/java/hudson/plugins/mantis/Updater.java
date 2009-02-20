package hudson.plugins.mantis;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.AbstractBuild.DependencyChange;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.scm.ChangeLogSet.Entry;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mantis update Logic.
 *
 * @author Seiji Sogabe
 */
final class Updater {

    private final MantisIssueUpdater property;

    Updater(final MantisIssueUpdater property) {
        this.property = property;
    }

    boolean perform(final AbstractBuild<?, ?> build, final BuildListener listener) {

        final PrintStream logger = listener.getLogger();

        final MantisSite site = MantisSite.get(build.getProject());
        if (site == null) {
            Utility.log(logger, Messages.Updater_NoMantisSite());
            build.setResult(Result.FAILURE);
            return true;
        }

        final String rootUrl = Hudson.getInstance().getRootUrl();
        if (rootUrl == null) {
            Utility.log(logger, Messages.Updater_NoHudsonUrl());
            build.setResult(Result.FAILURE);
            return true;
        }

        final int[] ids = findIssueIdsRecursive(build);
        if (ids.length == 0) {
            Utility.log(logger, Messages.Updater_NoIssuesFound());
            return true;
        }

        final boolean update = !build.getResult().isWorseThan(Result.UNSTABLE);
        if (!update) {
            // Keep id for next build
            Utility.log(logger, Messages.Updater_KeepMantisIssueIdsForNextBuild());
            build.addAction(new MantisCarryOverAction(ids));
        }

        final List<MantisIssue> issues = new ArrayList<MantisIssue>();
        for (final int id : ids) {
            try {
                final MantisIssue issue = site.getIssue(id);
                if (update) {
                    final String text = createUpdateText(build, rootUrl);
                    site.updateIssue(id, text, property.isKeepNotePrivate());
                    Utility.log(logger, Messages.Updater_Updating(id));
                }
                issues.add(issue);
            } catch (final MantisHandlingException e) {
                Utility.log(logger, Messages.Updater_FailedToAddNote(id, e.getMessage()));
				LOGGER.log(Level.WARNING, Messages.Updater_FailedToAddNote_StarckTrace(id), e);
            }
        }

        build.getActions().add(
                new MantisBuildAction(issues.toArray(new MantisIssue[issues.size()])));

        return true;
    }

    private String createUpdateText(final AbstractBuild<?, ?> build,
            final String rootUrl) {
        final String prjName = build.getProject().getName();
        final int prjNumber = build.getNumber();
        final String url = Util.encode(rootUrl + build.getUrl());
        final String text =
                Messages.Updater_IssueIntegrated(prjName, prjNumber, url);
        return text;
    }

    private int[] findIssueIdsRecursive(final AbstractBuild<?, ?> build) {
        final List<Integer> ids = new ArrayList<Integer>();

        final Run<?, ?> prev = build.getPreviousBuild();
        if (prev != null) {
            final MantisCarryOverAction action =
                    prev.getAction(MantisCarryOverAction.class);
            if (action != null) {
                for (int id : action.getIDs()) {
                    ids.add(id);
                }
            }
        }

        ids.addAll(findIssuesIds(build));

        for (final DependencyChange depc : build.getDependencyChanges(
                build.getPreviousBuild()).values()) {
            for (final AbstractBuild<?, ?> b : depc.getBuilds()) {
                ids.addAll(findIssuesIds(b));
            }
        }

        final int[] array = new int[ids.size()];
        for (int i = 0, size = ids.size(); i < size; i++) {
            array[i] = ids.get(i);
        }

        return array;
    }

    private List<Integer> findIssuesIds(final AbstractBuild<?, ?> build) {
        final List<Integer> ids = new ArrayList<Integer>();
        final MantisProjectProperty mpp =
            build.getParent().getProperty(MantisProjectProperty.class);
        if (mpp == null || mpp.getSite() == null) {
            return ids;
        }
        final Pattern pattern = mpp.getRegExp();
        for (final Entry change : build.getChangeSet()) {
            final Matcher matcher = pattern.matcher(change.getMsg());
            while (matcher.find()) {
                ids.add(Integer.valueOf(matcher.group()));
            }
        }
        return ids;
    }

	private static final Logger LOGGER = Logger.getLogger(Updater.class.getName());
}
