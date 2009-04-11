package hudson.plugins.mantis.changeset;

import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.scm.ChangeLogSet.Entry;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

/**
 * AbstractChangeSet
 * @author Seiji Sogabe
 * @since 0.7.0
 */
public abstract class AbstractChangeSet implements ChangeSet, Serializable {

    protected int id;

    protected AbstractBuild<?, ?> build;

    protected Entry entry;

    public AbstractChangeSet(final int id, final AbstractBuild<?, ?> build,
            final Entry entry) {
        this.id = id;
        this.build = build;
        this.entry = entry;
    }
    
    public int getId() {
        return id;
    }

    public abstract String createChangeLog();

    protected RepositoryBrowser getRepositoryBrowser() {
        if (build == null) {
            return null;
        }
        final SCM scm = build.getProject().getScm();
        return scm.getBrowser();
    }

    protected String getChangeSetLink() {
        final RepositoryBrowser browser = getRepositoryBrowser();
        if (browser == null) {
            return UNKNOWN_CHANGESETLINK;
        }

        String link = UNKNOWN_CHANGESETLINK;
        try {
            @SuppressWarnings("unchecked")
            final URL url = browser.getChangeSetLink(entry);
            if (url != null) {
                link = url.toString();
            }
        } catch (final IOException e) {
            // OK
        }
        return link;
    }

    protected String getAuthor() {
        final User user = entry.getAuthor();
        return (user == null) ? UNKNOWN_AUTHOR : user.getId();
    }

    protected String getMsg() {
        return entry == null ? UNKNOWN_MSG : entry.getMsg();
    }
}
