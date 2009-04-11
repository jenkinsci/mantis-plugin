package hudson.plugins.mantis.changeset;

/**
 * This changeSet has only id.
 * 
 * @author Seiji Sogabe
 * @since 0.7.0
 */
public class CompatibleChangeSet extends AbstractChangeSet {

    private static final long serialVersionUID = 1L;

    public CompatibleChangeSet(final int id) {
        super(id, null, null);
    }

    @Override
    public String createChangeLog() {
        return "";
    }
}
