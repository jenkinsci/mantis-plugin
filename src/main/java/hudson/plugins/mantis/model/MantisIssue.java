package hudson.plugins.mantis.model;

/**
 * One Mantis issue.
 *
 * @author Seiji Sogabe
 */
import java.io.Serializable;

public final class MantisIssue implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private String summary;

    private String description;

    private MantisProject project;

    private MantisCategory category;
    
    private MantisViewState viewState;
    
    private String status;



    public int getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public MantisCategory getCategory() {
        return category;
    }

    public MantisProject getProject() {
        return project;
    }
    
    public String getStatus() {
        return status;
    }

    public MantisIssue(final int id, final String summary, final String status) {
        this.id = id;
        this.summary = summary;
        this.status = status;
    }

    public MantisViewState getViewState() {
        return viewState;
    }

    /*  Mantis Issue Constructor */
    
    public MantisIssue(MantisProject project, MantisCategory category, String summary, 
            String description, MantisViewState viewState, String status) {
        this.summary = summary;
        this.description = description;
        this.project = project;
        this.category = category;
        this.viewState = viewState;
        this.status  = status;
    }
}
