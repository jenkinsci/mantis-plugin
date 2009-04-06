package hudson.plugins.mantis;

import hudson.model.InvisibleAction;

/**
 * Remembers Mantis IDs that need to be updated later, when we get a successfull build.
 * 
 * @deprecated
 * @see MantisCarryOverChangeSetAction
 * @author Seiji Sogabe
 */
public final class MantisCarryOverAction extends InvisibleAction {

    private static final long serialVersionUID = 1L;

    private final String ids;

    public MantisCarryOverAction(final int[] ids) {
        this.ids = Utility.join(ids, ",");
    }

    public int[] getIDs() {
        return Utility.tokenize(ids, ",");
    }

}
