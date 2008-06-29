package hudson.plugins.mantis.soap;

import hudson.plugins.mantis.MantisHandlingException;
import hudson.plugins.mantis.MantisSite;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.plugins.mantis.model.MantisNote;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisProperties;
import org.apache.axis.client.Stub;

public final class MantisSession {

    private static final String END_POINT = "api/soap/mantisconnect.php";

    private final MantisConnectPortType portType;

    private final MantisSite site;

    public static MantisSession create(final MantisSite site)
            throws MantisHandlingException {
        MantisConnectPortType portType;
        try {
            final URL endpoint = new URL(site.getUrl(), END_POINT);
            final MantisConnectLocator locator = new MantisConnectLocator();
            portType = locator.getMantisConnectPort(endpoint);

            // Basic Authentication if they are specified
            if (site.getBasicUserName() != null && site.getBasicPassword() != null) {
                ((Stub) portType).setUsername(site.getBasicUserName());
                ((Stub) portType).setPassword(site.getBasicPassword());
            }
            // Support https 
            // Allowing unsigned server certs
            AxisProperties.setProperty("axis.socketSecureFactory", 
                    "org.apache.axis.components.net.SunFakeTrustSocketFactory");
  
        } catch (final ServiceException e) {
            throw new MantisHandlingException(e);
        } catch (final MalformedURLException e) {
            throw new MantisHandlingException(e);
        }

        return new MantisSession(site, portType);
    }

    private MantisSession(final MantisSite site, final MantisConnectPortType portType) {
        this.site = site;
        this.portType = portType;
    }

    public String getConfigString(final String key) throws MantisHandlingException {
        String configString;
        try {
            configString =
                    portType.mc_config_get_string(site.getUserName(), site.getPassword(),
                            key);
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }

        return configString;
    }

    public MantisIssue getIssue(final Long id) throws MantisHandlingException {
        IssueData data;
        try {
            data =
                    portType.mc_issue_get(site.getUserName(), site.getPassword(),
                            BigInteger.valueOf(id));
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }

        return new MantisIssue(id, data.getSummary());
    }

    public void addNote(final Long id, final MantisNote note)
            throws MantisHandlingException {
        final IssueNoteData data = new IssueNoteData();
        data.setText(note.getText());
        data.setView_state(new ObjectRef(BigInteger
                .valueOf(note.getViewState().getCode()), null));

        try {
            portType.mc_issue_note_add(site.getUserName(), site.getPassword(), BigInteger
                    .valueOf(id), data);
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }
    }
}
