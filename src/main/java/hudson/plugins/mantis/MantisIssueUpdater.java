package hudson.plugins.mantis;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;

import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Parses changelog for Mantis issue IDs and updates Mantis issues.
 * 
 * @author Seiji Sogabe
 */
public final class MantisIssueUpdater extends Recorder {

    private final boolean keepNotePrivate;

    private final boolean recordChangelog;
    
    @DataBoundConstructor
    public MantisIssueUpdater(final boolean keepNotePrivate, final boolean recordChangelog) {
        this.keepNotePrivate = keepNotePrivate;
        this.recordChangelog = recordChangelog;
    }

    public boolean isKeepNotePrivate() {
        return keepNotePrivate;
    }

    public boolean isRecordChangelog() {
        return recordChangelog;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        final Updater updater = new Updater(this);
        return updater.perform(build, listener);
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(MantisIssueUpdater.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.MantisIssueUpdater_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/mantis/help.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public Publisher newInstance(final StaplerRequest req, final JSONObject formData) {
            return req.bindJSON(MantisIssueUpdater.class, formData);
        }
    }
}
