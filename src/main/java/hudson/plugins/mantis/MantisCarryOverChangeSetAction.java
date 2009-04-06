package hudson.plugins.mantis;

import hudson.model.InvisibleAction;
import java.util.Collections;
import java.util.List;

/**
 * Remembers Mantis IDs and changesSet that need to be updated later, when we get a successfull build.
 * @since 0.7
 * @author sogabe
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
