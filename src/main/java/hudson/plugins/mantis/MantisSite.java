package hudson.plugins.mantis;

import hudson.Util;
import hudson.model.AbstractProject;

import java.net.MalformedURLException;
import java.net.URL;

import org.kohsuke.stapler.DataBoundConstructor;
import org.mantisbt.connect.IMCSession;
import org.mantisbt.connect.MCException;
import org.mantisbt.connect.axis.MCSession;
import org.mantisbt.connect.model.IIssue;
import org.mantisbt.connect.model.INote;
import org.mantisbt.connect.model.Note;

/**
 * Reperesents an external MAntis installation and configuration needed to access this
 * Mantis.
 * 
 * @author Seiji Sogabe
 */
public final class MantisSite {

    private static final String END_POINT = "api/soap/mantisconnect.php";

    private URL url;

    private final String userName;

    private final String password;

    public static MantisSite get(final AbstractProject<?, ?> p) {
        final MantisProjectProperty mpp = p.getProperty(MantisProjectProperty.class);
        if (mpp != null) {
            final MantisSite site = mpp.getSite();
            if (site != null) {
                return site;
            }
        }

        final MantisSite[] sites = MantisProjectProperty.DESCRIPTOR.getSites();
        if (sites.length == 1) {
            return sites[0];
        }

        return null;
    }

    public URL getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return url.toExternalForm();
    }

    @DataBoundConstructor
    public MantisSite(final URL url, final String userName, final String password) {
        if (!url.toExternalForm().endsWith("/")) {
            try {
                this.url = new URL(url.toExternalForm() + '/');
            } catch (final MalformedURLException e) {
                throw new AssertionError(e);
            }
        } else {
            this.url = url;
        }
        this.userName = Util.fixEmpty(userName);
        this.password = Util.fixEmpty(password);
    }

    public boolean isConnect() {
        try {
            final IMCSession session = createSession();
            session.getConfigString("default_language");
        } catch (final MantisHandlingException e) {
            return false;
        } catch (final MCException e) {
            return false;
        }

        return true;
    }

    public MantisIssue getIssue(final Long id) throws MantisHandlingException {
        IIssue issue;
        final IMCSession session = createSession();
        try {
            issue = session.getIssue(id);
        } catch (final MCException e) {
            throw new MantisHandlingException(e);
        }
        if (issue == null) {
            return null;
        }
        return new MantisIssue(id, issue.getSummary());
    }

    public void addNote(final Long id, final String text, final boolean keepNotePrivate)
            throws MantisHandlingException {
        final IMCSession session = createSession();
        final INote note = new Note();
        note.setPrivate(keepNotePrivate);
        note.setText(text);
        try {
            session.addNote(id, note);
        } catch (final MCException e) {
            throw new MantisHandlingException(e);
        }
    }

    private IMCSession createSession() throws MantisHandlingException {
        if (userName == null || password == null) {
            throw new MantisHandlingException("user name or password is null.");
        }
        final URL mcUrl;
        try {
            mcUrl = new URL(url.toExternalForm() + END_POINT);
        } catch (final MalformedURLException e) {
            throw new AssertionError(e);
        }

        IMCSession session;
        try {
            session = new MCSession(mcUrl, userName, password);
        } catch (final MCException e) {
            throw new MantisHandlingException(e);
        }

        return session;
    }

}
