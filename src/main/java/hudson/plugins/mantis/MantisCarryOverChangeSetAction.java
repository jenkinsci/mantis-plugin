package hudson.plugins.mantis;

import hudson.model.InvisibleAction;
import hudson.plugins.mantis.changeset.ChangeSet;
import java.util.Collections;
import java.util.List;

/**
 * Remembers Mantis IDs and changesSet that need to be updated later, when we get a successfull build.
 * @author Seiji Sogabe
 * @since 0.7
 */
public class MantisCarryOverChangeSetAction extends InvisibleAction {

    private static final long serialVersionUID = 1L;

    private final List<ChangeSet> changeSets;

    public MantisCarryOverChangeSetAction(final List<ChangeSet> changeSet) {
        this.changeSets = changeSet;
    }

    public List<ChangeSet> getChangeSets() {
        return Collections.unmodifiableList(changeSets);
    }
}
