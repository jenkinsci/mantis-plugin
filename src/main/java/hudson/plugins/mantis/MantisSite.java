package hudson.plugins.mantis;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.plugins.mantis.model.MantisNote;
import hudson.plugins.mantis.model.MantisViewState;
import hudson.plugins.mantis.soap.MantisSession;

import hudson.util.Scrambler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Reperesents an external Mantis installation and configuration needed to access this
 * Mantis.
 *
 * @author Seiji Sogabe
 */
public final class MantisSite {

    /**
     * the root URL of Mantis installation.
     */
    private final URL url;

    /**
     * user name for Mantis installation.
     */
    private final String userName;

    /**
     * password for Mantis installation.
     */
    private final String password;

    /**
     * user name for Basic Authentication.
     */
    private final String basicUserName;

    /**
     * password for Basic Authentication.
     */
    private final String basicPassword;

    /**
     * HTTP-Proxy Host.
     */
    private final String proxyHost;

    /**
     * HTTP-Proxy Port.
     */
    private final String proxyPort;

    /**
     * HTTP-Proxy Username.
     */
    private final String proxyUserName;

    /**
     * HTTP-Proxy Password which is scrambled.
     */
    private final String proxyPassword;
    
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

    public String getBasicUserName() {
        return basicUserName;
    }

    public String getBasicPassword() {
        return basicPassword;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public String getProxyUserName() {
        return proxyUserName;
    }

    public String getProxyPassword() {
        return Scrambler.descramble(proxyPassword);
    }

    @DataBoundConstructor
    public MantisSite(final URL url,
            final String userName, final String password,
            final String basicUserName, final String basicPassword,
            final String proxyHost, final String proxyPort,
            final String proxyUserName, final String proxyPassword) {
        if (!url.toExternalForm().endsWith("/")) {
            try {
                this.url = new URL(url.toExternalForm() + '/');
            } catch (final MalformedURLException e) {
                throw new AssertionError(e);
            }
        } else {
            this.url = url;
        }

        this.userName = Util.fixEmptyAndTrim(userName);
        this.password = Util.fixEmptyAndTrim(password);
        this.basicUserName = Util.fixEmptyAndTrim(basicUserName);
        this.basicPassword = Util.fixEmptyAndTrim(basicPassword);
        this.proxyHost = Util.fixEmptyAndTrim(proxyHost);
        this.proxyPort = Util.fixEmptyAndTrim(proxyPort);
        this.proxyUserName = Util.fixEmptyAndTrim(proxyUserName);
        this.proxyPassword = Scrambler.scramble(Util.fixEmptyAndTrim(proxyPassword));
    }

    public boolean isConnect() {
        final String urlString = url.toExternalForm();
        try {
            final MantisSession session = createSession();
            session.getConfigString("default_language");
        } catch (final MantisHandlingException e) {
            LOGGER.log(Level.WARNING,
                    Messages.MantisSite_FailedToConnectToMantis(urlString, e.getMessage()));
            return false;
        }

        LOGGER.log(Level.INFO,
                Messages.MantisSite_SucceedInConnectingToMantis(urlString));
        return true;
    }

    public MantisIssue getIssue(final int id) throws MantisHandlingException {
        final MantisSession session = createSession();
        return session.getIssue(id);
    }

    public void updateIssue(final int id, final String text, final boolean keepNotePrivate)
            throws MantisHandlingException {

        final MantisViewState viewState = keepNotePrivate ? MantisViewState.PRIVATE
                : MantisViewState.PUBLIC;
        final MantisNote note = new MantisNote(text, viewState);

        final MantisSession session = createSession();
        session.addNote(id, note);
    }

    private MantisSession createSession() throws MantisHandlingException {
        return MantisSession.create(this);
    }

    private static final Logger LOGGER = Logger.getLogger(MantisSite.class.getName());
}
