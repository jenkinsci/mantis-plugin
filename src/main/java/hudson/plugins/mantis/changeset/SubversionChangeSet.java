package hudson.plugins.mantis.changeset;

import hudson.model.AbstractBuild;
import hudson.scm.EditType;
import hudson.scm.SubversionChangeLogSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ChangeSet of subversion.
 *
 * @author Seiji Sogabe
 * @since 0.7
 */
public class SubversionChangeSet extends AbstractChangeSet {

    private static final long serialVersionUID = 1L;

    public SubversionChangeSet(final int id, final AbstractBuild<?, ?> build,
            final SubversionChangeLogSet.LogEntry entry) {
        super(id, build, entry);
    }

    @Override
    public String createChangeLog() {
        final StringBuilder text = new StringBuilder();
        text.append(Messages.ChangeSet_Revision(getRevision(), getChangeSetLink()));
        text.append(CRLF);
        text.append(Messages.ChangeSet_Author(getAuthor()));
        text.append(CRLF);
        text.append(Messages.ChangeSet_Log(getMsg()));
        text.append(CRLF);
        text.append(Messages.ChangeSet_ChangedPaths_Header());
        text.append(CRLF);
        for (final AffectedPath path : getAffectedPaths()) {
            text.append(Messages.ChangeSet_ChangedPaths_Path(path.getMark(), path.getPath()));
            text.append(CRLF);
        }
        text.append(CRLF);
        return text.toString();
    }

    protected String getRevision() {
        return String.valueOf(((SubversionChangeLogSet.LogEntry) entry).getRevision());
    }

    private List<AffectedPath> getAffectedPaths() {
        final List<AffectedPath> paths = new ArrayList<AffectedPath>();
        for (final SubversionChangeLogSet.Path path : ((SubversionChangeLogSet.LogEntry) entry).getPaths()) {
            paths.add(new AffectedPath(path.getEditType(), path.getValue()));
        }
        return Collections.unmodifiableList(paths);
    }

    private static class AffectedPath {

        private static final String MARK_ADD = "A";

        private static final String MARK_DELETE = "D";

        private static final String MARK_EDIT = "M";

        private static final String MARK_UNKNOWN = " ";

        private final String mark;

        private final String path;

        public AffectedPath(final EditType type, final String path) {
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
    }
}
