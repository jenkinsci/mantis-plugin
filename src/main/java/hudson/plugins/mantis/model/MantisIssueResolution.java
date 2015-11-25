package hudson.plugins.mantis.model;

/**
 * View State for Issue and Note.
 *
 * @author Seiji Sogabe
 */
public enum MantisIssueResolution {

    FIXED(20);

    private int code;

    MantisIssueResolution(final int code) {
        this.code = code;
    }

    public static MantisIssueResolution fromCode(int code) {
        switch (code) {
            case 20:
                return FIXED;
            default:
                return null;
        }
    }

    public int getCode() {
        return code;
    }
}
