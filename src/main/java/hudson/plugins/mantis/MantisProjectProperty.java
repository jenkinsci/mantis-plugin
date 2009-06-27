package hudson.plugins.mantis;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.plugins.mantis.MantisSite.MantisVersion;
import hudson.util.CopyOnWriteList;
import hudson.util.FormFieldValidator;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Associates {@link AbstractProject} with {@link MantisSite}.
 *
 * @author Seiji Sogabe
 */
public final class MantisProjectProperty extends JobProperty<AbstractProject<?, ?>> {

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    private static final String ISSUE_ID_STRING = "%ID%";

    private static final String DEFAULT_PATTERN = "issue #?" + ISSUE_ID_STRING;

    private final String siteName;

    private final String pattern;

    private final String regex;

    private Pattern regexpPattern;

    private final boolean linkEnabled;

    @DataBoundConstructor
    public MantisProjectProperty(final String siteName, final String pattern, final String regex, final boolean linkEnabled) {
        final String name = (siteName != null) ? siteName : defaultSiteName();
        this.siteName = Util.fixEmptyAndTrim(name);
        this.pattern = Util.fixEmptyAndTrim(pattern);
        this.regex = Util.fixEmptyAndTrim(regex);
        this.regexpPattern = (this.regex != null) ? Pattern.compile(this.regex) : createRegexp(this.pattern);
        this.linkEnabled = linkEnabled;
    }

    public String getSiteName() {
        return siteName;
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

    @Override
    public JobPropertyDescriptor getDescriptor() {
        return DESCRIPTOR;
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
        buf.append(")");
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
            // only administrator allowed
            Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
            MantisProjectProperty mpp = req.bindParameters(MantisProjectProperty.class, "mantis.");
            if (mpp.siteName == null) {
                mpp = null;
            }
            return mpp;
        }

        @Override
        public boolean configure(final StaplerRequest req) {
            // only administrator allowed
            Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
            sites.replaceBy(req.bindParametersToList(MantisSite.class, "mantis."));
            save();
            return true;
        }

        public void doLoginCheck(final StaplerRequest req, final StaplerResponse res) throws IOException, ServletException {
            // only administrator allowed
            Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
            
            new FormFieldValidator(req, res, false) {
                @Override
                protected void check() throws IOException, ServletException {
                    final String url = Util.fixEmptyAndTrim(req.getParameter("url"));
                    if (url == null) {
                        error(Messages.MantisProjectProperty_MantisUrlMandatory());
                        return;
                    }
                    
                    final String user = Util.fixEmptyAndTrim(req.getParameter("user"));
                    final String pass = Util.fixEmptyAndTrim(req.getParameter("pass"));
                    final String bUser = Util.fixEmptyAndTrim(req.getParameter("buser"));
                    final String bPass = Util.fixEmptyAndTrim(req.getParameter("bpass"));
                    final String ver = Util.fixEmptyAndTrim(req.getParameter("version"));
                    
                    MantisVersion version = MantisVersion.getVersionSafely(ver, MantisVersion.V110);
                    
                    final MantisSite site = new MantisSite(new URL(url), version.name(), user, pass, bUser, bPass);
                    if (!site.isConnect()) {
                        error(Messages.MantisProjectProperty_UnableToLogin());
                        return;
                    }
                    ok();
                }
            }.process();
        }

        public void doPatternCheck(final StaplerRequest req, final StaplerResponse res) throws IOException, ServletException {
            // only administrator allowed
            Hudson.getInstance().checkPermission(Hudson.ADMINISTER);

            new FormFieldValidator(req, res, false) {
                @Override
                protected void check() throws IOException, ServletException {
                    final String pattern = Util.fixEmptyAndTrim(req.getParameter("pattern"));
                    if (pattern == null) {
                        ok();
                        return;
                    }
                    if (pattern.indexOf(ISSUE_ID_STRING) == -1) {
                        error(Messages.MantisProjectProperty_InvalidPattern(ISSUE_ID_STRING));
                        return;
                    }
                    ok();
                }
            }.process();
        }
    }
}
