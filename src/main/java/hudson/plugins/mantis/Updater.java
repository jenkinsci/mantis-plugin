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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mantis update Logic.
 * 
 * @author Seiji Sogabe
 */
final class Updater {

    private static final Pattern ISSUE_PATTERN =
            Pattern.compile("(?<=\\bissue #?)(\\d+)(?>\\b)", Pattern.CASE_INSENSITIVE);

    private final MantisIssueUpdater property;

    Updater(final MantisIssueUpdater property) {
        this.property = property;
    }

    boolean perform(final AbstractBuild<?, ?> build, final BuildListener listener) {

        final PrintStream logger = listener.getLogger();

        final MantisSite site = MantisSite.get(build.getProject());
        if (site == null) {
            logger.println(Messages.Updater_NoMantisSite());
            build.setResult(Result.FAILURE);
            return true;
        }

        final String rootUrl = Hudson.getInstance().getRootUrl();
        if (rootUrl == null) {
            logger.println(Messages.Updater_NoHudsonUrl());
            build.setResult(Result.FAILURE);
            return true;
        }

        final Set<Long> ids = findIssueIdsRecursive(build);
        if (ids.isEmpty()) {
            return true;
        }

        final boolean update = !build.getResult().isWorseThan(Result.UNSTABLE);
        if (!update) {
            // Keep id for next build
            build.addAction(new MantisCarryOverAction(ids.toArray(new Long[ids.size()])));
        }

        final List<MantisIssue> issues = new ArrayList<MantisIssue>();
        try {
            for (final Long id : ids) {
                final MantisIssue issue = site.getIssue(id);
                if (issue == null) {
                    continue;
                }
                if (update) {
                    final String text = createUpdateText(build, rootUrl);
                    site.updateIssue(id, text, property.isKeepNotePrivate());
                    logger.println(Messages.Updater_Updating(id));
                }
                issues.add(issue);
            }
        } catch (final MantisHandlingException e) {
            logger.println(Messages.Updater_FailedToAddNote());
            logger.println(e);
            build.setResult(Result.FAILURE);
            return true;
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

    private Set<Long> findIssueIdsRecursive(final AbstractBuild<?, ?> build) {
        final Set<Long> ids = new HashSet<Long>();

        final Run<?, ?> prev = build.getPreviousBuild();
        if (prev != null) {
            final MantisCarryOverAction action =
                    prev.getAction(MantisCarryOverAction.class);
            if (action != null) {
                ids.addAll(Arrays.asList(action.getIDs()));
            }
        }

        ids.addAll(findIssuesIds(build));

        for (final DependencyChange depc : build.getDependencyChanges(
                build.getPreviousBuild()).values()) {
            for (final AbstractBuild<?, ?> b : depc.getBuilds()) {
                ids.addAll(findIssuesIds(b));
            }
        }

        return ids;
    }

    private Set<Long> findIssuesIds(final AbstractBuild<?, ?> build) {
        final Set<Long> ids = new HashSet<Long>();

        for (final Entry change : build.getChangeSet()) {
            final Matcher matcher = ISSUE_PATTERN.matcher(change.getMsg());
            while (matcher.find()) {
                ids.add(Long.valueOf(matcher.group()));
            }
        }
        return ids;
    }
}
