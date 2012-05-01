package hudson.plugins.mantis.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Mantis Project
 *
 * @author Seiji Sogabe
 */
public class MantisProject implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int NONE = -1;

    private final int id;

    private final String name;

    private final List<MantisProject> subProjects = new ArrayList<MantisProject>();

    public MantisProject(int id) {
        this(id, null);
    }

    public MantisProject(int id, String name) {
        this(id, name, Collections.EMPTY_LIST);
    }

    public MantisProject(int id, String name, List<MantisProject> subProjects) {
        this.id = id;
        this.name = name;
        this.subProjects.addAll(subProjects);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<MantisProject> getSubProjects() {
        return subProjects;
    }

    public void addSubProjectAll(List<MantisProject> projects) {
        subProjects.addAll(projects);
    }

    public void addSubProject(MantisProject project) {
        subProjects.add(project);
    }
}
