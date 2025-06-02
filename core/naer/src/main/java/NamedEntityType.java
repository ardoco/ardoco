/**
 * Represents the type of a {@link NamedEntity}.
 */
public enum NamedEntityType {
    COMPONENT("C"),
    INTERFACE("I");
    // weitere...

    private final String prefix;

    NamedEntityType(String prefix) {
        this.prefix = prefix;
    }

    public static NamedEntityType fromPrefix(String prefix) {
        for (NamedEntityType type : NamedEntityType.values()) {
            if (type.prefix.equals(prefix)) return type;
        }
        //todo logging
        return null;
    }
}
