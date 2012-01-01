package hudson.plugins.mantis.soap.mantis110;

import hudson.plugins.mantis.MantisHandlingException;
import hudson.plugins.mantis.MantisSite;
import hudson.plugins.mantis.model.MantisCategory;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.plugins.mantis.model.MantisNote;
import hudson.plugins.mantis.model.MantisProject;
import hudson.plugins.mantis.soap.AbstractMantisSession;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.rpc.ServiceException;
import org.apache.axis.AxisProperties;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Stub;

@Deprecated
public final class MantisSessionImpl extends AbstractMantisSession {

    private final MantisConnectPortType portType;

    public MantisSessionImpl(final MantisSite site) throws MantisHandlingException {
        LOGGER.info("Mantis version is 1.1.X");
        this.site = site;
        try {
            final URL endpoint = new URL(site.getUrl(), END_POINT);
            final MantisConnectLocator locator = new MantisConnectLocator();

            // Set Handler
            final EngineConfiguration config = createClientConfig();
            locator.setEngineConfiguration(config);
            locator.setEngine(new AxisClient(config));

            portType = locator.getMantisConnectPort(endpoint);

            // Basic Authentication if they are specified
            if (site.getBasicUserName() != null && site.getBasicPassword() != null) {
                ((Stub) portType).setUsername(site.getBasicUserName());
                ((Stub) portType).setPassword(site.getBasicPassword());
            }
            // Support https
            // Allowing unsigned server certs
            AxisProperties.setProperty("axis.socketSecureFactory",
                    "org.apache.axis.components.net.SunFakeTrustSocketFactory");

        } catch (final ServiceException e) {
            throw new MantisHandlingException(e);
        } catch (final MalformedURLException e) {
            throw new MantisHandlingException(e);
        }
    }

    public MantisIssue getIssue(final int id) throws MantisHandlingException {
        IssueData data;
        try {
            data =
                    portType.mc_issue_get(site.getUserName(), site.getPassword(), BigInteger.valueOf(id));
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }

        return new MantisIssue(id, data.getSummary());
    }

    public void addNote(final int id, final MantisNote note)
            throws MantisHandlingException {
        final IssueNoteData data = new IssueNoteData();
        data.setText(note.getText());
        data.setView_state(new ObjectRef(BigInteger.valueOf(note.getViewState().getCode()), null));

        try {
            portType.mc_issue_note_add(site.getUserName(), site.getPassword(), BigInteger.valueOf(id), data);
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }
    }

    public String getVersion() throws MantisHandlingException {
        String version;
        try {
            version = portType.mc_version();
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }
        return version;
    }

    public List<MantisProject> getProjects() throws MantisHandlingException {
        List<MantisProject> projects = new ArrayList<MantisProject>();
        ProjectData[] data;
        try {
            data = portType.mc_projects_get_user_accessible(site.getUserName(), site.getPassword());
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }
        for (ProjectData p : data) {
            MantisProject mp = new MantisProject(p.getId().intValue(), p.getName(), subProjects(p));
            projects.add(mp);
        }
        return projects;
    }

    private List<MantisProject> subProjects(ProjectData p) {
        List<MantisProject> list = new ArrayList<MantisProject>();
        ProjectData[] subs = p.getSubprojects();
        for (ProjectData sub : subs) {
            MantisProject mp = new MantisProject(sub.getId().intValue(), sub.getName(), subProjects(sub));
            list.add(mp);
        }
        return list;
    }

    public List<MantisCategory> getCategories(int projectId) throws MantisHandlingException {
        List<MantisCategory> categories = new ArrayList<MantisCategory>();
        String[] list;
        try {
            list = portType.mc_project_get_categories(site.getUserName(), site.getPassword(), BigInteger.valueOf(projectId));
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }
        for (String category : list) {
            categories.add(new MantisCategory(category));
        }
        return categories;
    }

    public int addIssue(MantisIssue issue) throws MantisHandlingException {
        IssueData data = new IssueData();
        MantisProject project = issue.getProject();
        if (project == null) {
            throw new MantisHandlingException("project is missing.");
        }
        MantisCategory category = issue.getCategory();
        if (category == null) {
            throw new MantisHandlingException("category is missing.");
        }

        ObjectRef pRef = new ObjectRef(BigInteger.valueOf(project.getId()), project.getName());

        data.setProject(pRef);
        data.setCategory(category.getName());
        data.setSummary(issue.getSummary());
        data.setDescription(issue.getDescription());

        BigInteger addedIssueNo = null;
        try {
            addedIssueNo = portType.mc_issue_add(site.getUserName(), site.getPassword(), data);
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }

        return addedIssueNo.intValue();
    }

    private static final Logger LOGGER = Logger.getLogger(MantisSessionImpl.class.getName());
}
