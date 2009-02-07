package hudson.plugins.mantis.soap;

import hudson.Util;
import java.util.logging.Logger;
import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

/**
 * SOAP LogGandler.
 * @author Seiji Sogabe
 */
public class LogHandler extends BasicHandler {

	private static final long serialVersionUID = 1L;

	public void invoke(final MessageContext ctx) throws AxisFault {

		if (ctx == null) {
			return;
		}

		final Message req = ctx.getRequestMessage();
		final Message res = ctx.getResponseMessage();

		if (req != null) {
			LOGGER.fine("Request: " + Util.escape(req.getSOAPPartAsString()));
		} else {
			LOGGER.fine("Request: (null)");
		}

		if (res != null) {
			LOGGER.fine("Response: " + Util.escape(res.getSOAPPartAsString()));
		} else {
			LOGGER.fine("Response: (null)");
		}

	}

	private static final Logger LOGGER = Logger.getLogger(LogHandler.class.getName());
}
