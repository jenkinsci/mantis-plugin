package hudson.plugins.mantis.changeset;

import hudson.model.AbstractBuild;
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
public class SubversionChangeSet extends DefaultChangeSet {

    private static final long serialVersionUID = 1L;

    public SubversionChangeSet(final int id, final AbstractBuild<?, ?> build,
            final SubversionChangeLogSet.LogEntry entry) {
        super(id, build, entry);
    }

    @Override
    public List<AffectedPath> getAffectedPaths() {
        final List<AffectedPath> paths = new ArrayList<AffectedPath>();
        for (final SubversionChangeLogSet.Path path : ((SubversionChangeLogSet.LogEntry) entry).getPaths()) {
            paths.add(new AffectedPath(path.getEditType(), path.getValue()));
        }
        return Collections.unmodifiableList(paths);
    }

    @Override
    public String getRevision() {
        return String.valueOf(((SubversionChangeLogSet.LogEntry) entry).getRevision());
    }
}
