package edu.kit.kastel.mcse.ardoco.secdreqan;

public enum EntityLabel {

    COMPONENT_LONG("Component (l)"), COMPONENT_SHORT("Component (s)"), COMPONENT_C("Component (c)"), SHORT_SEQUENCE("short sequence (s)"), LONG_SEQUENCE(
            "long sequences (l)");

    private final String label;

    private EntityLabel(String label) {
        this.label = label;
    }

    public static EntityLabel fromString(String label) {
        for (EntityLabel b : EntityLabel.values()) {
            if (b.label.equalsIgnoreCase(label)) {
                return b;
            }
        }
        return null;
    }
}
