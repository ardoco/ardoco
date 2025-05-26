import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the occurrence of a component in a text.
 */
public class ComponentOccurrence {
    private final String componentName;
    private final int sentenceNumber;

    /**
     * Creates a new {@link ComponentOccurrence} instance.
     *
     * @param componentName  name of the component
     * @param sentenceNumber number of sentence where the component is mentioned
     */
    public ComponentOccurrence(String componentName, int sentenceNumber) {
        this.componentName = componentName;
        this.sentenceNumber = sentenceNumber;
    }

    /**
     * Parses a textual representation of a list of component occurrences to the actual list.
     *
     * @param str format must be "componentName1,sentenceNumber1\n componentName2,sentenceNumber2\n ...", lines not matching this format will be skipped with a warning
     * @return a set of {@link ComponentOccurrence} instances
     */
    public static Set<ComponentOccurrence> parse(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(str.split("\n")).map(String::trim).filter(line -> !line.isEmpty()).map(line -> {
            try {
                String[] parts = line.split(",");
                if (parts.length != 2) {
                    System.err.println("Warning: Skipping malformed line: \"" + line + "\"");
                    return null;
                }
                return new ComponentOccurrence(parts[0], Integer.parseInt(parts[1]));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.err.println("Warning: Skipping malformed line: \"" + line + "\"");
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public String getComponentName() {
        return componentName;
    }

    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public String toString() {
        return "{" + componentName + "," + sentenceNumber + "}\n";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;
        ComponentOccurrence that = (ComponentOccurrence) o;
        return this.getSentenceNumber() == that.getSentenceNumber() && this.getComponentName().equalsIgnoreCase(that.getComponentName()); //TODO hier AI namensvergleich oder so einbauen? oder schon davor mit AI angleichen und dann hier harter vgl(?)
    }

    @Override
    public int hashCode() {
        return Objects.hash(getComponentName().toLowerCase(), getSentenceNumber()); //TODO achtung hier auch casing anpassen
    }
}