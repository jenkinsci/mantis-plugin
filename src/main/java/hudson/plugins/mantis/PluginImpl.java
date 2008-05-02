package hudson.plugins.mantis;

import hudson.Plugin;
import hudson.model.Jobs;
import hudson.tasks.BuildStep;

/**
 * Mantis Plugin.
 *
 * @author Seiji Sogabe
 * @plugin
 */
public final class PluginImpl extends Plugin {

    private final MantisLinkAnnotator annotator = new MantisLinkAnnotator();

    @Override
    public void start() throws Exception {
        annotator.register();
        BuildStep.PUBLISHERS.addRecorder(MantisIssueUpdater.DESCRIPTOR);
        Jobs.PROPERTIES.add(MantisProjectProperty.DESCRIPTOR);
    }

    @Override
    public void stop() throws Exception {
        annotator.unregister();
    }

}
