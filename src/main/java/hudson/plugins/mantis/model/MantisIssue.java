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

    public MantisIssue(final int id, final String summary) {
        this.id = id;
        this.summary = summary;
    }

    public MantisIssue(MantisProject project, MantisCategory category, String summary, String description) {
        this.summary = summary;
        this.description = description;
        this.project = project;
        this.category = category;
    }
}
