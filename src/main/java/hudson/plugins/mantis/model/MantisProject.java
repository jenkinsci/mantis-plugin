package hudson.plugins.mantis.model;

import java.io.Serializable;

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

    public MantisProject(int id) {
        this(id, null);
    }
    
    public MantisProject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
