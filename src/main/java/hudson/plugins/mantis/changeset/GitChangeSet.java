package hudson.plugins.mantis.changeset;

import hudson.model.AbstractBuild;
import java.util.Collection;

/**
 * ChangeSet of Git.
 *
 * @author Seiji Sogabe
 * @since 0.7.1
 */
public class GitChangeSet extends AbstractChangeSet<hudson.plugins.git.GitChangeSet> {

    private static final long serialVersionUID = 1L;

    public GitChangeSet(final int id, final AbstractBuild<?, ?> build,
            final hudson.plugins.git.GitChangeSet entry) {
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
        for (final String path : getAffectedPaths()) {
            text.append(Messages.ChangeSet_ChangedPaths_Path("", path));
            text.append(CRLF);
        }
        text.append(CRLF);
        return text.toString();
    }

    protected String getRevision() {
        return String.valueOf(entry.getId());
    }

    private Collection<String> getAffectedPaths() {
        return entry.getAffectedPaths();
    }

}
