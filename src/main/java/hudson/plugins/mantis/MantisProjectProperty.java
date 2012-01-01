package hudson.plugins.mantis;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.plugins.mantis.MantisSite.MantisVersion;
import hudson.plugins.mantis.model.MantisCategory;
import hudson.plugins.mantis.model.MantisProject;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Associates {@link AbstractProject} with {@link MantisSite}.
 *
 * @author Seiji Sogabe
 */
public final class MantisProjectProperty extends JobProperty<AbstractProject<?, ?>> {

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
    private static final String ISSUE_ID_STRING = "%ID%";
    private static final String DEFAULT_PATTERN = "issue #?" + ISSUE_ID_STRING;
    private final String siteName;
    private final int projectId;
    private final String category;
    private final String pattern;
    private final String regex;
    private Pattern regexpPattern;
    private final boolean linkEnabled;

    @DataBoundConstructor
    public MantisProjectProperty(String siteName, int projectId, String category,
            String pattern, String regex, boolean linkEnabled) {
        final String name = (siteName != null) ? siteName : defaultSiteName();
        this.siteName = Util.fixEmptyAndTrim(name);
        this.projectId = projectId;
        this.category = Util.fixEmptyAndTrim(category);
        this.pattern = Util.fixEmptyAndTrim(pattern);
        this.regex = Util.fixEmptyAndTrim(regex);
        this.regexpPattern = (this.regex != null) ? Pattern.compile(this.regex) : createRegexp(this.pattern);
        this.linkEnabled = linkEnabled;
    }

    public String getSiteName() {
        return siteName;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getCategory() {
        return category;
    }

    public String getPattern() {
        return pattern;
    }

    public String getRegex() {
        return regex;
    }

    public Pattern getRegexpPattern() {
        // If project configuration has not saved after upgrading to 0.8.0,
        // return default issue id pattern.
        return (regexpPattern != null) ? regexpPattern : createRegexp(pattern);
    }

    public boolean isLinkEnabled() {
        return linkEnabled;
    }

    public MantisSite getSite() {
        final MantisSite[] sites = DESCRIPTOR.getSites();
        if (siteName == null && sites.length > 0) {
            return sites[0];
        }
        for (final MantisSite site : sites) {
            if (site.getName().equals(siteName)) {
                return site;
            }
        }
        return null;
    }

    private String defaultSiteName() {
        final MantisSite[] sites = DESCRIPTOR.getSites();
        if (sites.length > 0) {
            return sites[0].getName();
        }
        return null;
    }

    private Pattern createRegexp(final String p) {
        final StringBuffer buf = new StringBuffer();
        buf.append("(?<=");
        if (p != null) {
            buf.append(Utility.escapeRegexp(p));
        } else {
            buf.append(DEFAULT_PATTERN);
        }
        buf.append(')');
        final String pt = buf.toString().replace(ISSUE_ID_STRING, ")(\\d+)(?=");
        return Pattern.compile(pt);
    }

    public static final class DescriptorImpl extends JobPropertyDescriptor {

        private final CopyOnWriteList<MantisSite> sites = new CopyOnWriteList<MantisSite>();

        public DescriptorImpl() {
            super(MantisProjectProperty.class);
            load();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean isApplicable(final Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        @Override
        public String getDisplayName() {
            return Messages.MantisProjectProperty_DisplayName();
        }

        public MantisSite[] getSites() {
            return sites.toArray(new MantisSite[0]);
        }

        public MantisVersion[] getMantisVersions() {
            return MantisSite.MantisVersion.values();
        }

        @Override
        public JobProperty<?> newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
            MantisProjectProperty mpp = req.bindJSON(MantisProjectProperty.class, formData);
            if (mpp.siteName == null) {
                mpp = null;
            }
            return mpp;
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject formData) {
            sites.replaceBy(req.bindParametersToList(MantisSite.class, "mantis."));
            save();
            return true;
        }

        public ListBoxModel doFillSiteNameItems() {
            ListBoxModel m = new ListBoxModel();
            for (MantisSite site : getSites()) {
                m.add(site.getName());
            }
            return m;
        }

        public ListBoxModel doFillProjectIdItems(@QueryParameter String siteName) {
            ListBoxModel model = new ListBoxModel();
            model.add("-", String.valueOf(MantisProject.NONE));

            MantisSite site = null;
            for (final MantisSite s : sites) {
                if (s.getName().equals(siteName)) {
                    site = s;
                    break;
                }
            }
            if (site == null) {
                return model;
            }

            List<MantisProject> projects;
            try {
               projects = site.getProjects();
            } catch (MantisHandlingException e) {
                return model;
            }
            for (MantisProject p : projects) {
               model.add(p.getName(), "" + p.getId());
               for (MantisProjectItem sub : subProjects(p, 1)) {
                   model.add(sub.name, sub.id);
               }
            }

            return model;
        }

        private static class MantisProjectItem {

            public String name;

            public String id;

            public MantisProjectItem(String name, String id) {
                this.name = name;
                this.id = id;
            }
        }

        private List<MantisProjectItem> subProjects(MantisProject p, int depth) {
            List<MantisProjectItem> list = new ArrayList<MantisProjectItem>();
            for (MantisProject sub : p.getSubProjects()) {
                list.add(new MantisProjectItem(StringUtils.repeat("» ", depth) + sub.getName(), "" + sub.getId()));
                list.addAll(subProjects(sub, depth + 1));
            }
            return list;
        }

        public ListBoxModel doFillCategoryItems(@QueryParameter String siteName, @QueryParameter int projectId) {
            ListBoxModel model = new ListBoxModel();
            model.add("-", MantisCategory.None);
            if (projectId == MantisProject.NONE) {
                return model;
            }

            MantisSite site = null;
            for (final MantisSite s : sites) {
                if (s.getName().equals(siteName)) {
                    site = s;
                    break;
                }
            }
            if (site == null) {
                return model;
            }

            List<MantisCategory> categories;
            try {
                categories = site.getCategories(projectId);
            } catch (MantisHandlingException e) {
                return model;
            }
            for (MantisCategory category : categories) {
                model.add(category.getName());
            }
            return model;
        }

        public FormValidation doCheckLogin(final StaplerRequest req, final StaplerResponse res)
                throws IOException, ServletException {
            // only administrator allowed
            Hudson.getInstance().checkPermission(Hudson.ADMINISTER);

            final String url = Util.fixEmptyAndTrim(req.getParameter("url"));
            if (url == null) {
                return FormValidation.error(Messages.MantisProjectProperty_MantisUrlMandatory());
            }
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                return FormValidation.error(Messages.MantisProjectProperty_MalformedURL());
            }

            final String user = Util.fixEmptyAndTrim(req.getParameter("user"));
            final String pass = Util.fixEmptyAndTrim(req.getParameter("pass"));
            final String bUser = Util.fixEmptyAndTrim(req.getParameter("buser"));
            final String bPass = Util.fixEmptyAndTrim(req.getParameter("bpass"));
            final String ver = Util.fixEmptyAndTrim(req.getParameter("version"));

            MantisVersion version = MantisVersion.getVersionSafely(ver, MantisVersion.V120);

            final MantisSite site = new MantisSite(new URL(url), version.name(), user, pass, bUser, bPass);
            if (!site.isConnect()) {
                return FormValidation.error(Messages.MantisProjectProperty_UnableToLogin());
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckPattern(@AncestorInPath final AbstractProject<?, ?> project,
                @QueryParameter final String value) throws IOException, ServletException {
            project.checkPermission(Job.CONFIGURE);
            final String p = Util.fixEmptyAndTrim(value);
            if (p != null && p.indexOf(ISSUE_ID_STRING) == -1) {
                return FormValidation.error(Messages.MantisProjectProperty_InvalidPattern(ISSUE_ID_STRING));
            }

            return FormValidation.ok();
        }
    }
}
