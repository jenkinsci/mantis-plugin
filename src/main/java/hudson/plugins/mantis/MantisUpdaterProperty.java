package hudson.plugins.mantis;

/**
 * Mantis update property.
 *
 * @author Seiji Sogabe
 */
final class MantisUpdaterProperty {

    private boolean keepNotePrivate = false;

    boolean isKeepNotePrivate() {
        return keepNotePrivate;
    }

    void setKeepNotePrivate(final boolean keepNotePrivate) {
        this.keepNotePrivate = keepNotePrivate;
    }

}
