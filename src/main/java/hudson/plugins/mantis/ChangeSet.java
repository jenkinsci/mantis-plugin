package hudson.plugins.mantis;

import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.scm.ChangeLogSet.Entry;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import hudson.scm.SubversionChangeLogSet;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * ChangeSet
 * @author Seiji Sogabe
 */
public class ChangeSet implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String UNKNOWN_USER = "-";

    private static final String UNKNOWN_MSG = "-";

    private static final String UNKNOWN_REVISION = "-";

    private static final String UNKNOWN_ChangeSetLink = "";

    private final int id;

    private final String changeSetLink;

    private final String revision;

    private final String author;

    private final String msg;

    private final List<AffectedPath> affectedPaths = new ArrayList<AffectedPath>();

    public ChangeSet(final int id) {
        this.id = id;
        changeSetLink = UNKNOWN_ChangeSetLink;
        author = UNKNOWN_USER;
        msg = UNKNOWN_MSG;
        revision = UNKNOWN_REVISION;
    }

    public ChangeSet(final int id, final AbstractBuild build, final Entry change) {
        this.id = id;
        changeSetLink = createChangeSetLink(build, change);
        author = createAuthor(change);
        msg = change.getMsg();
        affectedPaths.addAll(createAffectedPaths(change));
        revision = createRevision(change);
    }

    public List<AffectedPath> getAffectedPaths() {
        return Collections.unmodifiableList(affectedPaths);
    }

    public String getAuthor() {
        return author;
    }

    public int getId() {
        return id;
    }

    public String getMsg() {
        return msg;
    }

    public String getRevision() {
        return revision;
    }

    public String getChangeSetLink() {
        return changeSetLink;
    }

    private String createAuthor(final Entry change) {
        final User user = change.getAuthor();
        if (user != null) {
            return user.getId();
        }
        
        return UNKNOWN_USER;
    }

    private String createRevision(final Entry change) {
        if (change instanceof SubversionChangeLogSet.LogEntry) {
            SubversionChangeLogSet.LogEntry svnEntry = (SubversionChangeLogSet.LogEntry) change;
            return String.valueOf(svnEntry.getRevision());
        }
        
        return UNKNOWN_REVISION;
    }

    private List<AffectedPath> createAffectedPaths(final Entry change) {
        final List<AffectedPath> paths = new ArrayList<AffectedPath>();

        if (change instanceof SubversionChangeLogSet.LogEntry) {
            final SubversionChangeLogSet.LogEntry svnEntry = (SubversionChangeLogSet.LogEntry) change;
            for (final SubversionChangeLogSet.Path path : svnEntry.getPaths()) {
                paths.add(new AffectedPath(path.getEditType(), path.getValue()));
            }
            return paths;
        }

        for (final String path : change.getAffectedPaths()) {
            paths.add(new AffectedPath(path));
        }
        return paths;
    }

    private String createChangeSetLink(final AbstractBuild build, final Entry change) {
        final SCM scm = build.getProject().getScm();
        final RepositoryBrowser browser = scm.getBrowser();
        if (browser == null) {
            return UNKNOWN_ChangeSetLink;
        }

        String link = UNKNOWN_ChangeSetLink;
        try {
            @SuppressWarnings("unchecked")
            final URL url = browser.getChangeSetLink(change);
            if (url != null) {
                link = url.toString();
            }
        } catch (final IOException e) {
            // OK
        }
        return link;
    }

    public static final class AffectedPath {

        private static final String MARK_ADD = "A";

        private static final String MARK_DELETE = "D";

        private static final String MARK_EDIT = "M";

        private static final String MARK_UNKNOWN = " ";

        private final EditType type;

        private final String mark;

        private final String path;

        public AffectedPath(final EditType type, final String path) {
            this.type = type;
            this.path = path;
            if (EditType.ADD.equals(type)) {
                mark = MARK_ADD;
            } else if (EditType.DELETE.equals(type)) {
                mark = MARK_DELETE;
            } else if (EditType.EDIT.equals(type)) {
                mark = MARK_EDIT;
            } else {
                mark = MARK_UNKNOWN;
            }
        }

        public AffectedPath(final String path) {
            this(null, path);
        }

        public String getMark() {
            return mark;
        }

        public String getPath() {
            return path;
        }

        public EditType getType() {
            return type;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
