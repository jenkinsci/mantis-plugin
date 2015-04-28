package hudson.plugins.mantis;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.mantis.changeset.ChangeSet;
import hudson.plugins.mantis.changeset.ChangeSetFactory;
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
    
    private boolean flag;

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

        final List<ChangeSet> chnageSets = findChangeSets(build);
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
                     //Add a System flag to control Changelogs on note
                     flag = false ;
                     final String text = createUpdateText(build, changeSet, rootUrl);
                     String text2 = "" ;
                    if(property.isNewNoteWithChangeLog()){
                        
                        flag = true;
                        text2 = createUpdateText(build, changeSet, rootUrl);
                       
                    }
                    site.updateIssue(changeSet.getId(), text, text2, property.isKeepNotePrivate(), property.isResolvedFilter(), property.isAddChangeLog(), property.isInvertSecondStateNote(), property.isNewNoteWithChangeLog());
                    Utility.log(logger, Messages.Updater_Updating(changeSet.getId()));
                }
                issues.add(issue);
            } catch (final MantisHandlingException e) {
                Utility.log(logger, Messages.Updater_FailedToAddNote(changeSet, e.getMessage()));
                LOGGER.log(Level.WARNING, Messages.Updater_FailedToAddNote_StarckTrace(changeSet), e);
            }
        }

        // build is not null, so mpp is not null
        MantisProjectProperty mpp = MantisProjectProperty.get(build);
        build.getActions().add(
                new MantisBuildAction(mpp.getRegexpPattern(), issues.toArray(new MantisIssue[0])));

        return true;
    }

    private String createUpdateText(final AbstractBuild<?, ?> build, final ChangeSet changeSet, final String rootUrl) {
        final String prjName = build.getProject().getName();
        final int prjNumber = build.getNumber();
        final String url = rootUrl + build.getUrl();
        
        final StringBuilder text = new StringBuilder();

        text.append(Messages.Updater_IssueIntegrated(prjName, prjNumber, url));
        text.append(CRLF).append(CRLF);
        
        if (property.isAddChangeLog() && property.isNewNoteWithChangeLog() == false ) 
        {           
            text.append(changeSet.createChangeLog());
        }
        
        if (property.isAddChangeLog()&& flag == true) 
        {           
            text.append(changeSet.createChangeLog());
            flag = false;
        }

        return text.toString();
    }

    private List<ChangeSet> findChangeSets(final AbstractBuild<?, ?> build) {
        final List<ChangeSet> chnageSets = new ArrayList<ChangeSet>();

        final Run<?, ?> prev = build.getPreviousBuild();
        if (prev != null) {
            final MantisCarryOverChangeSetAction changeSetAction = prev.getAction(MantisCarryOverChangeSetAction.class);
            if (changeSetAction != null) {
                for (final ChangeSet changeSet : changeSetAction.getChangeSets()) {
                    chnageSets.add(changeSet);
                }
            }
        }

        chnageSets.addAll(findChangeSetsFromSCM(build));

        return chnageSets;
    }

    private List<ChangeSet> findChangeSetsFromSCM(final AbstractBuild<?, ?> build) {
        final List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
        
        MantisProjectProperty mpp = MantisProjectProperty.get(build);
        final Pattern pattern = mpp.getRegexpPattern();
        for (final Entry change : build.getChangeSet()) {
            final Matcher matcher = pattern.matcher(change.getMsg());
            while (matcher.find()) {
                int id;
                try {
                    id = Integer.parseInt(matcher.group(1));
                } catch (final NumberFormatException e) {
                    // if id is not number, skip
                    LOGGER.log(Level.WARNING, Messages.Updater_IllegalMantisId(matcher.group(1)));
                    continue;
                }
                changeSets.add(ChangeSetFactory.newInstance(id, build, change));
            }
        }
        
        return changeSets;
    }
    
    private static final Logger LOGGER = Logger.getLogger(Updater.class.getName());
}
