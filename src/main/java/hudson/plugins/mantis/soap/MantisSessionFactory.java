package hudson.plugins.mantis.soap;

import hudson.plugins.mantis.MantisHandlingException;
import hudson.plugins.mantis.MantisSite;
import hudson.plugins.mantis.MantisSite.MantisVersion;
import hudson.plugins.mantis.soap.mantis120.MantisSessionImpl;

/**
 * Mantis Session factory.
 * @author Seiji Sogabe
 */
public class MantisSessionFactory {

    public static MantisSession getSession(final MantisSite site)
            throws MantisHandlingException {
        if (MantisVersion.V120.equals(site.getVersion())) {
            return new MantisSessionImpl(site);
        }
        return new hudson.plugins.mantis.soap.mantis110.MantisSessionImpl(site);
    }

}
