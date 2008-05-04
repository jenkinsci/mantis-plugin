package hudson.plugins.mantis.model;

import java.io.Serializable;

/**
 * One Mantis Note.
 *
 * @author Seiji Sogabe
 */
public final class MantisNote implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String text;

    private final MantisViewState viewState;

    public String getText() {
        return text;
    }

    public MantisViewState getViewState() {
        return viewState;
    }

    public MantisNote(final String text, final MantisViewState viewState) {
        this.text = text;
        this.viewState = viewState;
    }
}
