package hudson.plugins.mantis.model;

import java.io.Serializable;

/**
 * A categiry model
 * @author sogabe
 */
public class MantisCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String None = "Not Selected";
    
    private final String name;

    public MantisCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
