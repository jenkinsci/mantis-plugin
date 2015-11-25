package hudson.plugins.mantis.model;

public enum MantisIssueStatus {


    FEEDBACK(20),
    ACKNOWLEDGED(30),
    CONFIRMED(40),
    ASSIGNED(50),
    RESOLVED(80),
    CLOSED(90);

    private int code;

    MantisIssueStatus(final int code) {
        this.code = code;
    }


    public static MantisIssueStatus fromCode(int code) {
        switch (code) {
            case 20:
                return FEEDBACK;
            case 30:
                return ACKNOWLEDGED;
            case 40:
                return CONFIRMED;
            case 50:
                return ASSIGNED;
            case 80:
                return RESOLVED;
            case 90:
                return CLOSED;
            default:
                return null;
        }
    }

    public int getCode() {
        return code;
    }
}
