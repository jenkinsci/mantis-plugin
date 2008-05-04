package hudson.plugins.mantis;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.plugins.mantis.model.MantisNote;
import hudson.plugins.mantis.model.MantisViewState;
import hudson.plugins.mantis.soap.MantisConnectLocator;
import hudson.plugins.mantis.soap.MantisConnectPortType;
import hudson.plugins.mantis.soap.MantisSession;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.kohsuke.stapler.DataBoundConstructor;

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
            final MantisSession session = createSession();
            session.getConfigString("default_language");
        } catch (final MantisHandlingException e) {
            return false;
        }

        return true;
    }

    public MantisIssue getIssue(final Long id) throws MantisHandlingException {
        final MantisSession session = createSession();
        final MantisIssue issue = session.getIssue(id);
        if (issue == null) {
            return null;
        }
        return issue;
    }

    public void addNote(final Long id, final String text, final boolean keepNotePrivate)
            throws MantisHandlingException {

        final MantisViewState viewState = keepNotePrivate ? MantisViewState.PRIVATE
                : MantisViewState.PUBLIC;
        final MantisNote note = new MantisNote(text, viewState);

        final MantisSession session = createSession();
        session.addNote(id, note);
    }

    private MantisSession createSession() throws MantisHandlingException {
        if (userName == null || password == null) {
            throw new MantisHandlingException("user name or password is null.");
        }
        final URL endpoint;
        try {
            endpoint = new URL(url, END_POINT);
        } catch (final MalformedURLException e) {
            throw new AssertionError(e);
        }

        MantisConnectPortType portType;
        try {
            final MantisConnectLocator locator = new MantisConnectLocator();
            portType = locator.getMantisConnectPort(endpoint);
        } catch (final ServiceException e) {
            throw new MantisHandlingException(e);
        }

        return new MantisSession(this, portType);

    }

}
