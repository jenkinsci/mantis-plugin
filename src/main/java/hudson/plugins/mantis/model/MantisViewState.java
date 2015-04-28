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
    
    private MantisViewState(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    
    public static MantisViewState invertViewState(MantisViewState viewState)
    {
        if(viewState.getCode() == 10)
        {
            return MantisViewState.PRIVATE;
        }
        else
        {
            return MantisViewState.PUBLIC;
        }
    }
}
