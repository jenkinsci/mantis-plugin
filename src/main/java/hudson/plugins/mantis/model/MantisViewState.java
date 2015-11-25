package hudson.plugins.mantis.model;

/**
 * View State for Issue and Note.
 *
 * @author Seiji Sogabe
 */
public enum MantisViewState {

    PUBLIC(10),
    PRIVATE(50);

    private int code;
    
    MantisViewState(final int code) {
        this.code = code;
    }

    public static MantisViewState fromCode(int code) {
        switch (code) {
            case 10:
                return PUBLIC;
            case 50:
                return PRIVATE;
            default:
                return null;
        }
    }

    public int getCode() {
        return code;
    }
}
