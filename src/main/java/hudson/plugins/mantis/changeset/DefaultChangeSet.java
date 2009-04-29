package hudson.plugins.mantis.changeset;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet.Entry;
import java.util.ArrayList;
import java.util.List;

/**
 * Default ChangeSet
 *
 * @author Seiji Sogabe
 * @since 0.7
 */
public class DefaultChangeSet extends AbstractChangeSet<Entry> {

    private static final long serialVersionUID = 1L;

    public DefaultChangeSet(final int id, final AbstractBuild<?, ?> build, final Entry entry) {
        super(id, build, entry);
    }

    @Override
    public String createChangeLog() {
        final StringBuilder text = new StringBuilder();
        text.append(Messages.ChangeSet_Revision(UNKNOWN_REVISION, getChangeSetLink()));
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

    private List<String> getAffectedPaths() {
        final List<String> paths = new ArrayList<String>();
        for (final String path : entry.getAffectedPaths()) {
            paths.add(path);
        }
        return paths;
    }
}
