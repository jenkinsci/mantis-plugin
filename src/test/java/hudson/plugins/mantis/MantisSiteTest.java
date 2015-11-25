package hudson.plugins.mantis;

import hudson.plugins.mantis.model.MantisCategory;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.plugins.mantis.model.MantisProject;
import hudson.plugins.mantis.model.MantisViewState;

import java.net.URL;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Test class.
 *
 * @author Seiji Sogabe
 */
@Ignore(value = "not work behind a proxy")
public class MantisSiteTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private static final String MANTIS_URL = "http://bacons.ddo.jp/mantis/";
    
    private URL mantisUrl;
    
    private URL googleUrl;

    private MantisSite target;
    
    public MantisSiteTest() {
    }

    @Before
    public void setUp() throws Exception {
        mantisUrl = new URL(MANTIS_URL);
        googleUrl = new URL("http://www.google.com");
    }
    
    @Test
    public void testIsConnect() {
        target = createMantisSite();
        assertTrue(target.isConnect());
    }

    @Test
    public void testIsConnect_InvalidPassword() {
        target = new MantisSite(googleUrl, "V120", "jenkinsci", "dummy", null, null);
        assertFalse(target.isConnect());
    }
    
    @Test
    public void testIsConnect_NotMantis() {
        target = new MantisSite(googleUrl, "V120", "dummy", "dummy", null, null);
        assertFalse(target.isConnect());
    }
    
    @Test
    public void testGetIssue() throws MantisHandlingException {
        target = createMantisSite();
        MantisIssue issue = target.getIssue(232);
        
        assertNotNull(issue);
        assertEquals("for Jenkins Mantis Plugin", issue.getSummary());
    }

    @Test
    public void testGetIssue_NotFound() {
        target = createMantisSite();
        try {
            target.getIssue(99999);
            fail();
        } catch (MantisHandlingException e) {
            // OK
        }
    }
    
    @Test
    public void addNote() throws MantisHandlingException {
        target = createMantisSite();
        target.addNote(232, "Updated by Jenkins Mantis Plugin.", false);
    }
    
    @Test
    public void addIssue() throws MantisHandlingException {
        target = createMantisSite();
        String summary = "Build failed(Public)";
        String description = "Added by Jenkins Mantis Plugin.";
        MantisProject project = new MantisProject(2, "Jenkins Project");
        MantisCategory category = new MantisCategory("plugin");
        MantisIssue issue = new MantisIssue(0, project, category, summary, description, MantisViewState.PUBLIC, null,
                null);
        target.addIssue(issue);
    }

    @Test
    public void addIssue_private() throws MantisHandlingException {
        target = createMantisSite();
        String summary = "Build failed(Private)";
        String description = "Added by Jenkins Mantis Plugin.";
        MantisProject project = new MantisProject(2, "Jenkins Project");
        MantisCategory category = new MantisCategory("plugin");
        MantisIssue issue = new MantisIssue(0, project, category, summary, description, MantisViewState.PRIVATE, null,
                null);
        target.addIssue(issue);
    }
    
    private MantisSite createMantisSite() {
        return new MantisSite(mantisUrl, "V120", "jenkinsci", "jenkinsci", null, null);
    }
}
