package hudson.plugins.mantis.soap.mantis110;

import hudson.plugins.mantis.MantisHandlingException;
import hudson.plugins.mantis.MantisSite;
import hudson.plugins.mantis.model.*;
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
            if (site.getBasicUserName() != null && site.getPlainBasicPassword() != null) {
                ((Stub) portType).setUsername(site.getBasicUserName());
                ((Stub) portType).setPassword(site.getPlainBasicPassword());
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
                    portType.mc_issue_get(site.getUserName(), site.getPlainPassword(), BigInteger.valueOf(id));
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }

        return new MantisIssue(id, new MantisProject(data.getProject().getId().intValue(), data.getProject().getName()),
                new MantisCategory(data.getCategory()), data.getSummary(), data.getDescription(),
                MantisViewState.fromCode(data.getView_state().getId().intValue()),
                MantisIssueStatus.fromCode(data.getStatus().getId().intValue()));
    }

    public void addNote(final int id, final MantisNote note)
            throws MantisHandlingException {
        final IssueNoteData data = new IssueNoteData();
        data.setText(note.getText());
        data.setView_state(new ObjectRef(BigInteger.valueOf(note.getViewState().getCode()), null));

        try {
            portType.mc_issue_note_add(site.getUserName(), site.getPlainPassword(), BigInteger.valueOf(id), data);
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
            data = portType.mc_projects_get_user_accessible(site.getUserName(), site.getPlainPassword());
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
            list = portType.mc_project_get_categories(
                    site.getUserName(), site.getPlainPassword(), BigInteger.valueOf(projectId));
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }
        for (String category : list) {
            categories.add(new MantisCategory(category));
        }
        return categories;
    }

    public int addIssue(MantisIssue issue) throws MantisHandlingException {
        if (issue == null) {
            throw new MantisHandlingException("issue should not be null.");
        }
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
        ObjectRef viewStateRef = new ObjectRef(BigInteger.valueOf(issue.getViewState().getCode()), null);
        data.setView_state(viewStateRef);
        
        BigInteger addedIssueNo = null;
        try {
            addedIssueNo = portType.mc_issue_add(site.getUserName(), site.getPlainPassword(), data);
        } catch (final RemoteException e) {
            throw new MantisHandlingException(e);
        }

        return addedIssueNo.intValue();
    }

    @Override
    public void updateIssue(MantisIssue issue) throws MantisHandlingException {
        if (issue == null) {
            throw new MantisHandlingException("issue should not be null.");
        }
        IssueData data = new IssueData();

        MantisProject project = issue.getProject();
        if (project == null) {
            throw new MantisHandlingException("project is missing.");
        }
        MantisCategory category = issue.getCategory();
        if (category == null) {
            throw new MantisHandlingException("category is missing.");
        }

        data.setId(BigInteger.valueOf(issue.getId()));
        ObjectRef pRef = new ObjectRef(BigInteger.valueOf(project.getId()), project.getName());
        data.setProject(pRef);
        data.setCategory(category.getName());
        data.setSummary(issue.getSummary());
        data.setDescription(issue.getDescription());
        ObjectRef viewStateRef = new ObjectRef(BigInteger.valueOf(issue.getViewState().getCode()), null);
        data.setView_state(viewStateRef);
        ObjectRef statusRef = new ObjectRef(BigInteger.valueOf(issue.getStatus().getCode()), null);
        data.setStatus(statusRef);

        try {
            portType.mc_issue_update(site.getUserName(), site.getPlainPassword(), BigInteger.valueOf(issue.getId()), data);
        } catch (RemoteException e) {
            throw new MantisHandlingException(e);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(MantisSessionImpl.class.getName());
}
