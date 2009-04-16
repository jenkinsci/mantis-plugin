package hudson.plugins.mantis.changeset;

import hudson.scm.EditType;

/**
 * Utility.
 * @author Seiji Sogabe
 */
public final class ChangeSetUtil {

    private static final String MARK_ADD = "A";

    private static final String MARK_DELETE = "D";

    private static final String MARK_EDIT = "M";

    private static final String MARK_UNKNOWN = " ";

    private ChangeSetUtil() {
        // hide
    }

    public static String getEditTypeMark(final EditType type) {
        String mark = MARK_UNKNOWN;
        if (EditType.ADD.equals(type)) {
            mark = MARK_ADD;
        } else if (EditType.DELETE.equals(type)) {
            mark = MARK_DELETE;
        } else if (EditType.EDIT.equals(type)) {
            mark = MARK_EDIT;
        } 
        return mark;
    }
}
