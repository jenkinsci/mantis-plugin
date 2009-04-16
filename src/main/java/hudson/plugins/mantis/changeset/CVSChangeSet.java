package hudson.plugins.mantis.changeset;

import hudson.model.AbstractBuild;
import hudson.scm.CVSChangeLogSet;
import hudson.scm.CVSRepositoryBrowser;
import hudson.scm.EditType;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ChangeSet of CVS
 * @author Seiji Sogabe
 * @since 0.7
 */
public class CVSChangeSet extends AbstractChangeSet {

    private static final long serialVersionUID = 1L;

    public CVSChangeSet(final int id, final AbstractBuild<?, ?> build,
            final CVSChangeLogSet.CVSChangeLog entry) {
        super(id, build, entry);
    }

    @Override
    public String createChangeLog() {
        final StringBuilder text = new StringBuilder();
        text.append(Messages.ChangeSet_Author(getAuthor()));
        text.append(CRLF);
        text.append(Messages.ChangeSet_Log(getMsg()));
        text.append(CRLF);
        text.append(Messages.ChangeSet_ChangedPaths_Header());
        text.append(CRLF);
        for (final AffectedPath path : getAffectedPaths()) {
            text.append(Messages.ChangeSet_ChangedPaths_CVS_Path(
                    path.getMark(), path.getPath(), path.getRevision(), path.getDiffLink()));
            text.append(CRLF);
        }
        text.append(CRLF);
        return text.toString();
    }

    @Override
    protected String getChangeSetLink() {
        return UNKNOWN_CHANGESETLINK;
    }

    private List<AffectedPath> getAffectedPaths() {
        final List<AffectedPath> paths = new ArrayList<AffectedPath>();
        final CVSRepositoryBrowser browser = (CVSRepositoryBrowser) getRepositoryBrowser();
        for (final CVSChangeLogSet.File file : ((CVSChangeLogSet.CVSChangeLog) entry).getFiles()) {
            paths.add(new AffectedPath(file, browser));
        }
        return paths;
    }

    private static class AffectedPath {

        private final CVSChangeLogSet.File file;

        private final CVSRepositoryBrowser browser;

        public AffectedPath(final CVSChangeLogSet.File file,
                final CVSRepositoryBrowser browser) {
            this.file = file;
            this.browser = browser;
        }

        public String getMark() {
            final EditType type = file.getEditType();
            return ChangeSetUtil.getEditTypeMark(type);
        }

        public String getPath() {
            return file.getFullName();
        }

        public String getRevision() {
            return file.getPrevrevision();
        }

        public String getDiffLink() {
            if (browser == null) {
                return UNKNOWN_CHANGESETLINK;
            }
            URL link = null;
            try {
                link = browser.getDiffLink(file);
            } catch (final IOException e) {
                // OK
            }
            return (link == null) ? UNKNOWN_CHANGESETLINK : link.toExternalForm();
        }
    }
}
