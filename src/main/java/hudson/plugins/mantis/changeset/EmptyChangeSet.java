package hudson.plugins.mantis.changeset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This changeSet has only id.
 * 
 * @author Seiji Sogabe
 * @since 0.7.0
 */
public class EmptyChangeSet extends DefaultChangeSet {

    private static final long serialVersionUID = 1L;

    public EmptyChangeSet(final int id) {
        super(id, null, null);
    }

    @Override
    public String getRevision() {
        return UNKNOWN_REVISION;
    }

    @Override
    public String getChangeSetLink() {
        return UNKNOWN_ChangeSetLink;
    }

    @Override
    public String getAuthor() {
        return UNKNOWN_AUTHOR;
    }

    @Override
    public String getMsg() {
        return UNKNOWN_MSG;
    }

    @Override
    public List<AffectedPath> getAffectedPaths() {
        return Collections.unmodifiableList(new ArrayList<AffectedPath>());
    }
}
