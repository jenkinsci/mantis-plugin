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
import java.util.logging.Level;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Parses changelog for Mantis issue IDs and updates Mantis issues.
 * 
 * @author Seiji Sogabe
 */
public final class MantisIssueUpdater extends Recorder {

    private final  boolean keepNotePrivate;

    private  boolean addChangeLog;
    
    private final  boolean resolvedFilter;
    
    private  boolean NewNoteWithChangeLog;
    
    private  boolean InvertSecondStateNote;
    
    private  boolean ModeNewChangeLogNote;
    
    private  boolean ModeAddChangeLogNote;
    
    private  boolean ModeNewChangeLogNoteInvertedStatus;
    
    
    
    
    
    @DataBoundConstructor
    public MantisIssueUpdater(
             boolean keepNotePrivate,
             boolean addChangeLog,
             boolean resolvedFilter,
             boolean NewNoteWithChangeLog,
             boolean InvertSecondStateNote,
             boolean ModeNewChangeLogNote,
             boolean ModeAddChangeLogNote,
             boolean ModeNewChangeLogNoteInvertedStatus
        
    ) 
    {
        this.keepNotePrivate = keepNotePrivate;
        this.resolvedFilter = resolvedFilter;
        //----------------------------------------
        this.addChangeLog = addChangeLog;
        this.NewNoteWithChangeLog = NewNoteWithChangeLog;
        this.InvertSecondStateNote = InvertSecondStateNote;
        this.ModeNewChangeLogNote = ModeNewChangeLogNote;
        this.ModeAddChangeLogNote = ModeAddChangeLogNote;
        this.ModeNewChangeLogNoteInvertedStatus = ModeNewChangeLogNoteInvertedStatus;        
        SetMode();
    }
    
    
    public boolean isModeNewChangeLogNote(){
        return ModeNewChangeLogNote;
        
    }
    public boolean isModeAddChangeLogNote(){
        return ModeAddChangeLogNote;
        
    }
    public boolean isModeNewChangeLogNoteInvertedStatus(){
        return ModeNewChangeLogNoteInvertedStatus;
        
    }
    public boolean isKeepNotePrivate() {
        return keepNotePrivate;
    }

    public boolean isAddChangeLog() {
        return addChangeLog;
    }
    public boolean isResolvedFilter() {
        return resolvedFilter;
    }
    
     public boolean isNewNoteWithChangeLog() {
        return NewNoteWithChangeLog;
    }
     public boolean isInvertSecondStateNote() {
        return InvertSecondStateNote;
    }
    
    public void SetMode(){
        this.addChangeLog = false;
        this.NewNoteWithChangeLog = false;
        this.InvertSecondStateNote = false;
        
        // Exclusive Select if Mode 1 & Mode 2 are Selected only M1 be active   
        if(ModeAddChangeLogNote == true){
            //Set other mod to false
            this.ModeNewChangeLogNote = false;
            this.ModeNewChangeLogNoteInvertedStatus = false;    
        
            this.addChangeLog = true;

        }
        // Exclusive Select if Mode 1 & Mode 2 are Selected only M1 be active   
        if(ModeNewChangeLogNote == true){
            //Set other mod to false
            this.ModeAddChangeLogNote = false;
            this.ModeNewChangeLogNoteInvertedStatus = false;
        
            this.addChangeLog = true;
            this.NewNoteWithChangeLog = true;
        }
         // Exclusive Select if Mode 2 & Mode 3 are Selected only M2 be active          
        if(ModeNewChangeLogNoteInvertedStatus == true){
            //Set other mod to false
            this.ModeAddChangeLogNote = false;
            this.ModeNewChangeLogNote = false;

            this.addChangeLog = true;
            this.NewNoteWithChangeLog = true;
            this.InvertSecondStateNote = true;

        }
    }
    @Override
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
            req.bindJSON(this, formData.getJSONObject("fruit"));
            save();           
            return req.bindJSON(MantisIssueUpdater.class, formData);
        }

    }
}
