package hudson.plugins.mantis.soap;

import hudson.plugins.mantis.MantisHandlingException;
import hudson.plugins.mantis.MantisSite;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.plugins.mantis.model.MantisNote;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.Handler;
import org.apache.axis.SimpleChain;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.transport.http.HTTPTransport;

/**
 * Abstarct MantisSession class.
 * @author Seiji Sogabe
 */
public abstract class AbstractMantisSession implements MantisSession {

    protected static final String END_POINT = "api/soap/mantisconnect.php";

    protected MantisSite site;

    public abstract void addNote(int id, MantisNote note) throws MantisHandlingException;

    public abstract MantisIssue getIssue(int id) throws MantisHandlingException;

    public abstract String getVersion() throws MantisHandlingException;

    protected EngineConfiguration createClientConfig() {
        final SimpleProvider config = new SimpleProvider();
        final Handler handler = (Handler) new LogHandler();
        final SimpleChain reqChain = new SimpleChain();
        final SimpleChain resChain = new SimpleChain();

        reqChain.addHandler(handler);
        resChain.addHandler(handler);

        final Handler pivot = (Handler) new HTTPSender();
        final Handler transport = new SimpleTargetedChain(reqChain, pivot, resChain);
        config.deployTransport(HTTPTransport.DEFAULT_TRANSPORT_NAME, transport);

        return config;
    }
}
