package hudson.plugins.mantis;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.plugins.mantis.model.MantisNote;
import hudson.plugins.mantis.model.MantisViewState;
import hudson.plugins.mantis.soap.MantisSession;
import hudson.plugins.mantis.soap.MantisSessionFactory;

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
     * MantisVersion of Mantis.
     */
    private MantisVersion version = MantisVersion.V110;

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

    public MantisVersion getVersion() {
        return version;
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

    @DataBoundConstructor
    public MantisSite(final URL url, final String version, final String userName,
            final String password, final String basicUserName, final String basicPassword) {
        if (!url.toExternalForm().endsWith("/")) {
            try {
                this.url = new URL(url.toExternalForm() + '/');
            } catch (final MalformedURLException e) {
                throw new AssertionError(e);
            }
        } else {
            this.url = url;
        }
        this.version = MantisVersion.getVersionSafely(version, MantisVersion.V110);
        this.userName = Util.fixEmptyAndTrim(userName);
        this.password = Util.fixEmptyAndTrim(password);
        this.basicUserName = Util.fixEmptyAndTrim(basicUserName);
        this.basicPassword = Util.fixEmptyAndTrim(basicPassword);
    }

    public boolean isConnect() {
        final String urlString = url.toExternalForm();
        try {
            final MantisSession session = createSession();
            final String v = session.getVersion();
            LOGGER.info(Messages.MantisSite_DetectedVersion(v));
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
        return MantisSessionFactory.getSession(this);
    }

    public enum MantisVersion {
        /**
         * 1.1.X.
         */
        V110(Messages.MantisSite_MantisVersion_V110()),
        /**
         * 1.2.0a4 and later.
         */
        V120(Messages.MantisSite_MantisVersion_V120());

        private final String displayName;

        private MantisVersion(final String displayName) {
            this.displayName = displayName;
        }

        public static MantisVersion getVersionSafely(
                final String version, final MantisVersion def) {
            MantisVersion ret = def;
            for (final MantisVersion v : MantisVersion.values()) {
                if (v.name().equalsIgnoreCase(version)) {
                    ret = v;
                    break;
                }
            }
            return ret;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(MantisSite.class.getName());
}
