package hudson.plugins.mantis;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.CopyOnWriteList;
import hudson.util.FormFieldValidator;

import java.io.IOException;
import java.net.URL;

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

    private final String siteName;

    @DataBoundConstructor
    public MantisProjectProperty(final String siteName) {
        String name = siteName;
        if (siteName == null) {
            final MantisSite[] sites = DESCRIPTOR.getSites();
            if (sites.length > 0) {
                name = sites[0].getName();
            }
        }
        this.siteName = name;
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

        @Override
        public JobProperty<?> newInstance(final StaplerRequest req,
                final JSONObject formData) {
            MantisProjectProperty mpp = req.bindJSON(MantisProjectProperty.class,
                    formData);
            if (mpp.siteName == null) {
                mpp = null;
            }
            return mpp;
        }

        @Override
        public boolean configure(final StaplerRequest req) {
            sites.replaceBy(req.bindParametersToList(MantisSite.class, "mantis."));
            save();
            return true;
        }

        public void doUrlCheck(final StaplerRequest req, final StaplerResponse res)
                throws IOException, ServletException {
            new FormFieldValidator.URLCheck(req, res) {
                @Override
                protected void check() throws IOException, ServletException {
                    final String value = Util.fixEmptyAndTrim(request
                            .getParameter("value"));
                    if (value == null) {
                        error(Messages.MantisProjectProperty_MantisUrlMandatory());
                        return;
                    }
                    try {
                        open(new URL(value));
                    } catch (final IOException e) {
                        error(Messages.MantisProjectProperty_NotOpenUrl());
                        return;
                    }
                    ok();
                }
            }.process();
        }

        public void doLoginCheck(final StaplerRequest req, final StaplerResponse res)
                throws IOException, ServletException {
            new FormFieldValidator(req, res, false) {
                @Override
                protected void check() throws IOException, ServletException {
                    final String url = Util.fixEmptyAndTrim(req.getParameter("url"));
                    if (url == null) {
                        ok();
                        return;
                    }
                    final String user = req.getParameter("user");
                    final String pass = req.getParameter("pass");
                    final MantisSite site = new MantisSite(new URL(url), user, pass);
                    if (!site.isConnect()) {
                        error(Messages.MantisProjectProperty_UnableToLogin());
                        return;
                    }
                    ok();
                }
            }.process();
        }
    }
}
