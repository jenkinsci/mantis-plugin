package hudson.plugins.mantis.changeset;

import java.util.List;

/**
 * ChangeSet interface.
 * @author Seiji Sogabe
 * @since 0.7
 */
public interface ChangeSet {

    List<AffectedPath> getAffectedPaths();

    String getAuthor();

    String getChangeSetLink();

    int getId();

    String getMsg();

    String getRevision();
}
