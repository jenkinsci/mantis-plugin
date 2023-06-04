package hudson.plugins.mantis;

import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlForm;
import hudson.model.JobProperty;
import java.net.URL;
import jenkins.model.Jenkins;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

/**
 *
 * @author Seiji Sogabe
 */
public class MantisProjectPropertyConfigSubmitTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private WebClient webClient;

    @Before
    public void setUp() throws Exception {
        webClient = j.createWebClient();
        webClient.setCssEnabled(false);
        webClient.setThrowExceptionOnFailingStatusCode(false);
    }

    @Test
    public void testConfigSubmit_001() throws Exception {
        
        MantisProjectProperty.DescriptorImpl descriptor = new MantisProjectProperty.DescriptorImpl();
        MantisSite s = new MantisSite(new URL("http://localhost/mantis/"), "V110", "test", "test", "test", "test");
        descriptor.addSite(s);
        
        Jenkins.getInstance().getDescriptorList(JobProperty.class).add(descriptor);
        
        HtmlForm form = webClient.goTo("configure").getFormByName("config");
        
        assertEquals("http://localhost/mantis/", form.getInputByName("m.url").getValue());
        assertEquals("V110", form.getSelectByName("m.version").getSelectedOptions().get(0).getValue());
        assertEquals(s.getUserName(), form.getInputByName("m.userName").getValue());
        assertEquals(s.getSecretPassword().getEncryptedValue(), form.getInputByName("m.password").getValue());
        assertEquals(s.getBasicUserName(), form.getInputByName("m.basicUserName").getValue());
        assertEquals(s.getSecretBasicPassword().getEncryptedValue(), form.getInputByName("m.basicPassword").getValue());
        
    }

    @Test
    public void testConfigSubmit_002() throws Exception {
        
        MantisProjectProperty.DescriptorImpl descriptor = new MantisProjectProperty.DescriptorImpl();
        MantisSite s = new MantisSite(new URL("http://localhost/mantis/"), "V110", "test", "test", null, null);
        descriptor.addSite(s);
        
        Jenkins.getInstance().getDescriptorList(JobProperty.class).add(descriptor);
        
        HtmlForm form = webClient.goTo("configure").getFormByName("config");
        
        form.getInputByName("m.url").setValue("http://bacons.ddo.jp/mantis/");
        form.getSelectByName("m.version").setSelectedAttribute("V120", true);
        form.getInputByName("m.userName").setValue("mantis");
        form.getInputByName("m.password").setValue("mantis");
        form.getInputByName("m.basicUserName").setValue("mantis");
        form.getInputByName("m.basicPassword").setValue("mantis");
        
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        
        MantisSite[] sites = descriptor.getSites();
        assertNotNull(sites);
        assertEquals(1, sites.length);

        MantisSite site = sites[0];
        assertEquals(new URL("http://bacons.ddo.jp/mantis/"), site.getUrl());
        assertEquals(MantisSite.MantisVersion.V120, site.getVersion());
        assertEquals("mantis", site.getUserName());
        assertEquals("mantis", site.getPlainPassword());
        assertEquals("mantis", site.getBasicUserName());
        assertEquals("mantis", site.getPlainBasicPassword());
    }
}
