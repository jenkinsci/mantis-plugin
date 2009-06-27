package hudson.plugins.mantis.changeset;

import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.scm.CVSChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.scm.SubversionChangeLogSet;

/**
 * ChangeSet Factory.
 * @author Seiji Sogabe
 * @since 0.7
 */
public final class ChangeSetFactory {

    private ChangeSetFactory() {
        // hide default constructor
    }

    public static ChangeSet newInstance(final int id) {
        return new CompatibleChangeSet(id);
    }

    public static ChangeSet newInstance(final int id, final AbstractBuild<?, ?> build,
            final Entry entry) {
        if (build == null || entry == null) {
            throw new IllegalArgumentException();
        }
        
        final Hudson hudson = Hudson.getInstance();

        // CVS
        if (entry instanceof CVSChangeLogSet.CVSChangeLog) {
            return new CVSChangeSet(id, build, (CVSChangeLogSet.CVSChangeLog) entry);
        }
        // Subversion
        if (hudson.getPlugin("subversion") != null &&
             entry instanceof SubversionChangeLogSet.LogEntry) {
                return new SubversionChangeSet(id, build, (SubversionChangeLogSet.LogEntry) entry);
        }
        // Mercurial
        if (hudson.getPlugin("mercurial") != null &&
            entry instanceof hudson.plugins.mercurial.MercurialChangeSet) {
                return new MercurialChangeSet(id, build, (hudson.plugins.mercurial.MercurialChangeSet) entry);
        }
        // Git
        if (hudson.getPlugin("git") != null &&
            entry instanceof hudson.plugins.git.GitChangeSet) {
                return new GitChangeSet(id, build, (hudson.plugins.git.GitChangeSet) entry);
        }
        // else
        return new DefaultChangeSet(id, build, entry);
    }
}
