package hudson.plugins.mantis;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
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
        
        assertEquals("http://localhost/mantis/", form.getInputByName("m.url").getValueAttribute());
        assertEquals("V110", form.getSelectByName("m.version").getSelectedOptions().get(0).getValueAttribute());
        assertEquals(s.getUserName(), form.getInputByName("m.userName").getValueAttribute());
        assertEquals(s.getSecretPassword().getEncryptedValue(), form.getInputByName("m.password").getValueAttribute());
        assertEquals(s.getBasicUserName(), form.getInputByName("m.basicUserName").getValueAttribute());
        assertEquals(s.getSecretBasicPassword().getEncryptedValue(), form.getInputByName("m.basicPassword").getValueAttribute());
        
    }

    @Test
    public void testConfigSubmit_002() throws Exception {
        
        MantisProjectProperty.DescriptorImpl descriptor = new MantisProjectProperty.DescriptorImpl();
        MantisSite s = new MantisSite(new URL("http://localhost/mantis/"), "V110", "test", "test", null, null);
        descriptor.addSite(s);
        
        Jenkins.getInstance().getDescriptorList(JobProperty.class).add(descriptor);
        
        HtmlForm form = webClient.goTo("configure").getFormByName("config");
        
        form.getInputByName("m.url").setValueAttribute("http://bacons.ddo.jp/mantis/");
        form.getSelectByName("m.version").setSelectedAttribute("V120", true);
        form.getInputByName("m.userName").setValueAttribute("mantis");
        form.getInputByName("m.password").setValueAttribute("mantis");
        form.getInputByName("m.basicUserName").setValueAttribute("mantis");
        form.getInputByName("m.basicPassword").setValueAttribute("mantis");
        
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
