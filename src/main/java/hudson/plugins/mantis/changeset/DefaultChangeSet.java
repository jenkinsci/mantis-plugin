package hudson.plugins.mantis.changeset;

import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.scm.ChangeLogSet.Entry;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Default ChangeSet
 *
 * @author Seiji Sogabe
 * @since 0.7
 */
public class DefaultChangeSet implements ChangeSet, Serializable {

    private static final long serialVersionUID = 1L;

    protected static final String UNKNOWN_AUTHOR = "-";

    protected static final String UNKNOWN_MSG = "-";

    protected static final String UNKNOWN_REVISION = "-";

    protected static final String UNKNOWN_ChangeSetLink = "";

    protected int id;

    protected AbstractBuild<?, ?> build;

    protected Entry entry;

    public DefaultChangeSet(final int id, final AbstractBuild<?, ?> build, final Entry entry) {
        this.id = id;
        this.build = build;
        this.entry = entry;
    }

    public int getId() {
        return id;
    }

    public String getChangeSetLink() {
        final SCM scm = build.getProject().getScm();
        final RepositoryBrowser browser = scm.getBrowser();
        if (browser == null) {
            return UNKNOWN_ChangeSetLink;
        }

        String link = UNKNOWN_ChangeSetLink;
        try {
            @SuppressWarnings("unchecked")
            final URL url = browser.getChangeSetLink(entry);
            if (url != null) {
                link = url.toString();
            }
        } catch (final IOException e) {
            // OK
        }
        return link;
    }

    public String getAuthor() {
        final User user = entry.getAuthor();
        if (user != null) {
            return user.getId();
        }

        return UNKNOWN_AUTHOR;
    }

    public String getRevision() {
        return UNKNOWN_REVISION;
    }

    public String getMsg() {
        return entry.getMsg();
    }

    public List<AffectedPath> getAffectedPaths() {
        final List<AffectedPath> paths = new ArrayList<AffectedPath>();
        for (final String path : entry.getAffectedPaths()) {
            paths.add(new AffectedPath(path));
        }
        return Collections.unmodifiableList(paths);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
