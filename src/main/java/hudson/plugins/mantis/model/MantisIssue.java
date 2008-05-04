package hudson.plugins.mantis.model;

/**
 * One Mantis issue.
 *
 * @author Seiji Sogabe
 */
import java.io.Serializable;

public final class MantisIssue implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;

    private final String summary;

    public Long getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public MantisIssue(final Long id, final String summary) {
        this.id = id;
        this.summary = summary;
    }

}
