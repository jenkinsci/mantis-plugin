package hudson.plugins.mantis.scripts;

import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Result;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.xml.sax.InputSource;

/**
 * Jelly script content
 * 
 * @author Seiji Sogabe
 */
public class JellyScriptContent implements ScriptContent {

    private static final int BUFFER_SIZE = 16 * 1024;
    
    public String getContent(AbstractBuild<?, ?> build, Result result) throws IOException, InterruptedException {
        InputStream inputStream = null;
        String templateName = result.toString().toLowerCase();
        try {
            inputStream = getTemplateInputStream(templateName);
            return renderContent(build, inputStream);
        } catch (JellyException e) {
            LOGGER.log(Level.WARNING, "failed to parse jelly template.", e);
            return "JellyException: " + e.getMessage();
        } catch (FileNotFoundException e) {
            String missingTemplateError = generateMissingTemplate(templateName);
            return missingTemplateError;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private String generateMissingTemplate(String template) {
        return "Jelly script [" + template + "] was not found";
    }

    private InputStream getTemplateInputStream(String templateName) throws FileNotFoundException {
        // $JENKINS_HOME/mantis/scripts/templates/
        File templatesFolder = new File(Hudson.getInstance().getRootDir(), "mantis/scripts/templates/descriptions");
        File templateFile = new File(templatesFolder, templateName + ".jelly");
        if (templateFile.exists()) {
            return new FileInputStream(templateFile);
        }
        // bundled templates
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                "hudson/plugins/mantis/scripts/templates/descriptions/" + templateName + ".jelly");
        return inputStream;
    }

    private String renderContent(AbstractBuild<?, ?> build, InputStream inputStream)
            throws JellyException, IOException {
        JellyContext context = createContext(new ScriptContentBuildWrapper(build), build);
        Script script = context.compileScript(new InputSource(inputStream));
        if (script != null) {
            return convert(context, script);
        }
        return null;
    }

    private String convert(JellyContext context, Script script) throws JellyTagException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(BUFFER_SIZE);
        XMLOutput xmlOutput = null;
        try {
            xmlOutput = XMLOutput.createXMLOutput(output);
            script.run(context, xmlOutput);
            xmlOutput.flush();
        } finally {
            try {
                if (xmlOutput != null) {
                    xmlOutput.close();
                }
            } finally {
                output.close();
            }
        }
        return output.toString();
    }

    private JellyContext createContext(Object it, AbstractBuild<?, ?> build) {
        JellyContext context = new JellyContext();
        context.setVariable("it", it);
        context.setVariable("build", build);
        context.setVariable("project", build.getParent());
        context.setVariable("rooturl", Hudson.getInstance().getRootUrl());
        return context;
    }
    
    private static final Logger LOGGER = Logger.getLogger(JellyScriptContent.class.getName());
}
