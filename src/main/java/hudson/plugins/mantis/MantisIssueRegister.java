package hudson.plugins.mantis;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;

import hudson.model.Result;
import hudson.plugins.mantis.model.MantisCategory;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.plugins.mantis.model.MantisProject;
import hudson.plugins.mantis.scripts.JellyScriptContent;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.IOException;


import java.io.PrintStream;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * 
 * @author Seiji Sogabe
 */
public final class MantisIssueRegister extends Recorder {
    
    private String threshold;
    
    public static final String FAILURE = "failure";
    
    public static final String FAILUREORUNSTABL = "failureOrUnstable";
    
    @DataBoundConstructor
    public MantisIssueRegister(String threshold) {
        this.threshold = Util.fixEmptyAndTrim(threshold);
    }
    
    public String getThreshold() {
        return threshold;
    }
    
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        
        final PrintStream logger = listener.getLogger();
        
        if (!canProcess(build)) {
            return true;
        }
        
        MantisSite site = MantisSite.get(build.getProject());
        if (site == null) {
            Utility.log(logger, Messages.MantisIssueRegister_NoMantisSite());
            build.setResult(Result.FAILURE);
            return true;
        }
        
        int no;
        MantisIssue issue = createIssue(build);
        try {
            no = site.addIssue(issue);
            Utility.log(logger, "file ticket #" + no + "(" + getIssueURL(site, no) + ")");
        } catch (MantisHandlingException e) {
            build.setResult(Result.FAILURE);
            return true;
        }
        
        build.getActions().add(new MantisRegisterAction(site, no));
        
        return true;
    }
    
    private boolean canProcess(AbstractBuild<?, ?> build) {
        Result result = build.getResult();
        if (FAILURE.equals(threshold) && result.isBetterThan(Result.FAILURE)) {
            return false;
        } else if (FAILUREORUNSTABL.equals(threshold) && result.isBetterThan(Result.UNSTABLE)) {
            return false;
        }
        return true;
    }
    
    private String getIssueURL(MantisSite site, int no) {
        return site.getIssueLink(no);
    }
    
    private MantisIssue createIssue(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
        MantisProjectProperty mpp = build.getParent().getProperty(MantisProjectProperty.class);
        int projectId = mpp.getProjectId();
        String categoryName = mpp.getCategory();
        if (projectId == MantisProject.NONE || MantisCategory.None.equals(categoryName)) {
            return null;
        }
        
        MantisProject project = new MantisProject(projectId);
        MantisCategory category = new MantisCategory(categoryName);
        String summary = summary(build);
        String description = new JellyScriptContent().getContent(build, build.getResult());
        return new MantisIssue(project, category, summary, description);
    }
    
    private String summary(AbstractBuild<?, ?> build) {
        StringBuilder summary = new StringBuilder();
        summary.append(build.getParent().getName());
        summary.append(" build No." + build.getNumber());
        summary.append(" " + build.getResult().toString());
        return summary.toString();
    }
    
    @Extension(ordinal=1000)
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        
        public DescriptorImpl() {
            super(MantisIssueRegister.class);
        }
        
        @Override
        public String getDisplayName() {
            return Messages.MantisIssueRegister_DisplayName();
        }
        
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
    
    private static final Logger LOGGER = Logger.getLogger(MantisIssueRegister.class.getName());
}
