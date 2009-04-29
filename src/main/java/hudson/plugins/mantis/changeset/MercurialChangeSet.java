package hudson.plugins.mantis.changeset;

import hudson.model.AbstractBuild;
import hudson.scm.EditType;
import java.util.ArrayList;
import java.util.List;

/**
 * ChangeSet of Mecrutial.
 *
 * @author Seiji Sogabe
 * @since 0.7.1
 */
public class MercurialChangeSet extends AbstractChangeSet<hudson.plugins.mercurial.MercurialChangeSet> {

    private static final long serialVersionUID = 1L;

    public MercurialChangeSet(final int id, final AbstractBuild<?, ?> build,
            final hudson.plugins.mercurial.MercurialChangeSet entry) {
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
        return String.valueOf(entry.getRev()) + ":" + entry.getShortNode();
    }

    private List<AffectedPath> getAffectedPaths() {
        final List<AffectedPath> affectedPaths = new ArrayList<AffectedPath>();
        for (final EditType type : new EditType[] { EditType.ADD, EditType.EDIT, EditType.DELETE } ) {
            affectedPaths.addAll(getAffectedPathsByEditType(type));
        }
        return affectedPaths;
    }

    private List<AffectedPath> getAffectedPathsByEditType(final EditType type) {
        final List<AffectedPath> affectedPaths = new ArrayList<AffectedPath>();
        final List<String> paths = entry.getPaths(type);
        if (paths == null) {
            return affectedPaths;
        }
        for (final String path : paths) {
            affectedPaths.add(new AffectedPath(type, path));
        }
        return affectedPaths;
    }

    private static class AffectedPath {

        private final String mark;

        private final String path;

        public AffectedPath(final EditType type, final String path) {
            this.path = path;
            this.mark = ChangeSetUtil.getEditTypeMark(type);
        }

        public String getMark() {
            return mark;
        }

        public String getPath() {
            return path;
        }
    }
}
