package hudson.plugins.mantis.changeset;

import hudson.scm.EditType;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * File paths.
 *
 * @author Seiji Sogabe
 * @since 0.7
 */
public class AffectedPath {

    private static final String MARK_ADD = "A";

    private static final String MARK_DELETE = "D";

    private static final String MARK_EDIT = "M";

    private static final String MARK_UNKNOWN = " ";

    private final String mark;

    private final String path;

    public AffectedPath(final EditType type, final String path) {
        this.path = path;
        if (EditType.ADD.equals(type)) {
            mark = MARK_ADD;
        } else if (EditType.DELETE.equals(type)) {
            mark = MARK_DELETE;
        } else if (EditType.EDIT.equals(type)) {
            mark = MARK_EDIT;
        } else {
            mark = MARK_UNKNOWN;
        }
    }

    public AffectedPath(final String path) {
        this(null, path);
    }

    public String getMark() {
        return mark;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}