package hudson.plugins.mantis;

import hudson.plugins.mantis.model.MantisIssue;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class.
 * 
 * @author Seiji Sogabe
 */
public class MantisSiteTest {

    private static String MANTIS_URL = "http://bacons.ddo.jp/mantis/";
    
    private URL mantisUrl;
    
    private URL googleUrl;

    private MantisSite target;
    
    public MantisSiteTest() {
    }

    @Before
    public void setUp() throws MalformedURLException {
        mantisUrl = new URL(MANTIS_URL);
        googleUrl = new URL("http://www.google.com");
    }
    
    @After
    public void tearDown() {
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

    @Test(expected = MantisHandlingException.class)
    public void testGetIssue_NotFound() throws MantisHandlingException {
        target = createMantisSite();
        target.getIssue(99999);
    }
    
    @Test
    public void updateIssue() throws MantisHandlingException {
        target = createMantisSite();
        target.updateIssue(232, "Updated by Jenkins Mantis Plugin.", false);
    }
    
    private MantisSite createMantisSite() {
        return new MantisSite(mantisUrl, "V120", "jenkinsci", "jenkinsci", null, null);
    }
}
