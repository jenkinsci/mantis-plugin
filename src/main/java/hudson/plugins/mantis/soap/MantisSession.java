package hudson.plugins.mantis.soap;

import hudson.plugins.mantis.MantisHandlingException;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.plugins.mantis.model.MantisNote;

/**
 *
 * @author sogabe
 */
public interface MantisSession {

    void addNote(final int id, final MantisNote note) throws MantisHandlingException;

    String getVersion() throws MantisHandlingException;
    
    MantisIssue getIssue(final int id) throws MantisHandlingException;

}
