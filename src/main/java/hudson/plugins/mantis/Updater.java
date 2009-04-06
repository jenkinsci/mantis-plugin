package hudson.plugins.mantis;

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

    private static final String CRLF = System.getProperty("line.separator");
    
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

        final List<ChangeSet> chnageSets = findIssueIdsRecursive(build);
        if (chnageSets.isEmpty()) {
            Utility.log(logger, Messages.Updater_NoIssuesFound());
            return true;
        }

        final boolean update = !build.getResult().isWorseThan(Result.UNSTABLE);
        if (!update) {
            // Keep id for next build
            Utility.log(logger, Messages.Updater_KeepMantisIssueIdsForNextBuild());
            build.addAction(new MantisCarryOverChangeSetAction(chnageSets));
        }

        final List<MantisIssue> issues = new ArrayList<MantisIssue>();
        for (final ChangeSet changeSet : chnageSets) {
            try {
                final MantisIssue issue = site.getIssue(changeSet.getId());
                if (update) {
                    final String text = createUpdateText(build, changeSet, rootUrl);
                    site.updateIssue(changeSet.getId(), text, property.isKeepNotePrivate());
                    Utility.log(logger, Messages.Updater_Updating(changeSet));
                }
                issues.add(issue);
            } catch (final MantisHandlingException e) {
                Utility.log(logger, Messages.Updater_FailedToAddNote(changeSet, e.getMessage()));
                LOGGER.log(Level.WARNING, Messages.Updater_FailedToAddNote_StarckTrace(changeSet), e);
            }
        }

        build.getActions().add(
                new MantisBuildAction(issues.toArray(new MantisIssue[issues.size()])));

        return true;
    }

    private String createUpdateText(final AbstractBuild<?, ?> build, final ChangeSet changeSet, final String rootUrl) {
        final String prjName = build.getProject().getName();
        final int prjNumber = build.getNumber();
        final String url = rootUrl + build.getUrl();

        final StringBuilder text = new StringBuilder();
        text.append(Messages.Updater_IssueIntegrated(prjName, prjNumber, url));
        text.append(CRLF).append(CRLF);
        
        text.append(Messages.Updater_ChangeSet_Revision(changeSet.getRevision(), changeSet.getChangeSetLink())).append(CRLF);
        text.append(Messages.Updater_ChangeSet_Author(changeSet.getAuthor())).append(CRLF);
        text.append(Messages.Updater_ChangeSet_Log(changeSet.getMsg())).append(CRLF);
        text.append(Messages.Updater_ChangeSet_Files_Header()).append(CRLF);
        for (final ChangeSet.AffectedPath path : changeSet.getAffectedPaths()) {
            text.append(Messages.Updater_ChangeSet_Files_File(path.getMark(), path.getPath())).append(CRLF);
        }
        text.append(CRLF);
        return text.toString();
    }

    private List<ChangeSet> findIssueIdsRecursive(final AbstractBuild<?, ?> build) {
        final List<ChangeSet> chnageSets = new ArrayList<ChangeSet>();

        final Run<?, ?> prev = build.getPreviousBuild();
        if (prev != null) {
            final MantisCarryOverAction action = prev.getAction(MantisCarryOverAction.class);
            if (action != null) {
                for (int id : action.getIDs()) {
                    chnageSets.add(new ChangeSet(id));
                }
            }
            final MantisCarryOverChangeSetAction changeSetAction = prev.getAction(MantisCarryOverChangeSetAction.class);
            if (changeSetAction != null) {
                for (final ChangeSet changeSet : changeSetAction.getChangeSets()) {
                    chnageSets.add(changeSet);
                }
            }
        }

        chnageSets.addAll(findIssuesIds(build));

        for (final DependencyChange depc : build.getDependencyChanges(
                build.getPreviousBuild()).values()) {
            for (final AbstractBuild<?, ?> b : depc.getBuilds()) {
                chnageSets.addAll(findIssuesIds(b));
            }
        }

        return chnageSets;
    }

    private List<ChangeSet> findIssuesIds(final AbstractBuild<?, ?> build) {
        final List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
        final MantisProjectProperty mpp =
                build.getParent().getProperty(MantisProjectProperty.class);
        if (mpp == null || mpp.getSite() == null) {
            return changeSets;
        }
        final Pattern pattern = mpp.getRegExp();
        for (final Entry change : build.getChangeSet()) {
            final Matcher matcher = pattern.matcher(change.getMsg());
            while (matcher.find()) {
                changeSets.add(new ChangeSet(Integer.parseInt(matcher.group()), build, change));
            }
        }
        return changeSets;
    }

    private static final Logger LOGGER = Logger.getLogger(Updater.class.getName());
}
