package hudson.plugins.mantis.model;

/**
 * View State for Issue and Note.
 *
 * @author Seiji Sogabe
 */
public enum MantisViewState {

    PUBLIC() {
        public int getCode() {
            return 10;
        }
    },
    PRIVATE() {
        public int getCode() {
            return 50;
        }
    };


    abstract public int getCode();
}
