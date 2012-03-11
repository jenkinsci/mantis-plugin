package hudson.plugins.mantis;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import hudson.model.Hudson;
import hudson.model.JobProperty;
import java.net.URL;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 *
 * @author Seiji Sogabe
 */
public class MantisProjectPropertyConfigSubmitTest extends HudsonTestCase {

    private WebClient webClient;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        webClient = new WebClient();
        webClient.setCssEnabled(false);
        webClient.setThrowExceptionOnFailingStatusCode(false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConfigSubmit_001() throws Exception {
        
        MantisProjectProperty.DescriptorImpl descriptor = new MantisProjectProperty.DescriptorImpl();
        MantisSite s = new MantisSite(new URL("http://localhost/mantis/"), "V110", "test", "test", "test", "test");
        descriptor.addSite(s);
        
        Hudson.getInstance().getDescriptorList(JobProperty.class).add(descriptor);
        
        HtmlForm form = webClient.goTo("configure").getFormByName("config");
        
        assertEquals("http://localhost/mantis/", form.getInputByName("m.url").getValueAttribute());
        assertEquals("V110", form.getSelectByName("m.version").getSelectedOptions().get(0).getValueAttribute());
        assertEquals("test", form.getInputByName("m.userName").getValueAttribute());
        assertEquals("test", form.getInputByName("m.password").getValueAttribute());
        assertEquals("test", form.getInputByName("m.basicUserName").getValueAttribute());
        assertEquals("test", form.getInputByName("m.basicPassword").getValueAttribute());
        
    }

    public void testConfigSubmit_002() throws Exception {
        
        MantisProjectProperty.DescriptorImpl descriptor = new MantisProjectProperty.DescriptorImpl();
        MantisSite s = new MantisSite(new URL("http://localhost/mantis/"), "V110", "test", "test", null, null);
        descriptor.addSite(s);
        
        Hudson.getInstance().getDescriptorList(JobProperty.class).add(descriptor);
        
        HtmlForm form = webClient.goTo("configure").getFormByName("config");
        
        form.getInputByName("m.url").setValueAttribute("http://bacons.ddo.jp/mantis/");
        form.getSelectByName("m.version").setSelectedAttribute("V120", true);
        form.getInputByName("m.userName").setValueAttribute("mantis");
        form.getInputByName("m.password").setValueAttribute("mantis");
        form.getInputByName("m.basicUserName").setValueAttribute("mantis");
        form.getInputByName("m.basicPassword").setValueAttribute("mantis");
        
        submit(form);
        
        MantisSite[] sites = descriptor.getSites();
        assertNotNull(sites);
        assertEquals(1, sites.length);

        MantisSite site = sites[0];
        assertEquals(new URL("http://bacons.ddo.jp/mantis/"), site.getUrl());
        assertEquals(MantisSite.MantisVersion.V120, site.getVersion());
        assertEquals("mantis", site.getUserName());
        assertEquals("mantis", site.getPassword());
        assertEquals("mantis", site.getBasicUserName());
        assertEquals("mantis", site.getBasicPassword());
    }
}
