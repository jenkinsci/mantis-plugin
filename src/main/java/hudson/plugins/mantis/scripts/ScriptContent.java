package hudson.plugins.mantis.scripts;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import java.io.IOException;

/**
 * Script Content interface
 * @author Seiji Sogabe
 */
public interface ScriptContent {

    String getContent(AbstractBuild<?, ?> build, Result result) throws IOException, InterruptedException;
}
