package hudson.plugins.mantis;

/**
 * Mantis handling Exception.
 * 
 * @author Seiji Sogabe
 */
public final class MantisHandlingException extends Exception {

    private static final long serialVersionUID = 1L;

    public MantisHandlingException() {
        super();
    }

    public MantisHandlingException(final String message) {
        super(message);
    }

    public MantisHandlingException(final Throwable cause) {
        super(cause);
    }

    public MantisHandlingException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
