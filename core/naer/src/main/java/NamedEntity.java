import kotlin.Pair;

import java.util.HashSet;
import java.util.Set;


/**
 * Represents a named entity.
 */
public class NamedEntity {
    private String name;
    private NamedEntityType type;
    /**
     * alternative names of the entity, e.g., if the name is ambiguous
     */
    private Set<String> alternativeNames;
    /**
     * all occurrences of the entity in a text, including the sentence number (1-indexed) and the type of how the entity is referenced //todo ist es architektur mäßig jetzt schlecht hier von dem text zu reden obwohl der nicht von der Klasse aus referenziert wird oder so?
     */
    private Set<Pair<Integer, NamedEntityReferenceType>> sentenceOccurrences;


    public NamedEntity(String name, NamedEntityType type) {
        this.name = name;
        this.type = type;
    }

    //format must be like this! todo add javadoc
    //C:Database;UserDatabase,DB
    //o:1,d;3,c;8,d;11,c
    public static Set<NamedEntity> parse(String str) {//TODO tests für diese methode schreiben um die vielen Fälle abzudecken und robustness zu verbessern
        //System.out.println(str);
        if (str == null || str.trim().isEmpty()) {//warning
            return new HashSet<>();
        }

        Set<NamedEntity> result = new HashSet<>();
        String[] lines = str.split("\\r?\\n");

        NamedEntity currentEntity = null;
        boolean currentlyInEntityLine = true; //used to make sure the format is right (see Javadoc)

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(":", 2);
            if (parts.length != 2) continue; //todo warning oder vllt sogar abbrechen

            String prefix = parts[0].trim(); //todo check dass prefix!=null -> sonst error oder so [ALLE FEHLER DIE AUFTRETEN KÖNNEN DURCH EINE/ZWEI SMARTE LOG MESSAGES HANDLEN]
            String content = parts[1].trim();

            if (currentlyInEntityLine) { //the current line describes the entities name(s) and type:
                if (!Character.isUpperCase(prefix.charAt(0)))
                    continue; //todo logging dass das invalid input format ist und abbrechen + analog beim occurence teil

                NamedEntityType entityType = NamedEntityType.fromPrefix(prefix);

                String[] nameParts = content.split(";", 2);
                String entityName = nameParts[0].trim();

                currentEntity = new NamedEntity(entityName, entityType);
                result.add(currentEntity);

                if (nameParts.length > 1) {
                    String[] alternatives = nameParts[1].split(",");
                    for (String alternative : alternatives) {
                        currentEntity.addAlternativeName(alternative.trim());
                    }
                }

                currentlyInEntityLine = false; //get ready for next line
            } else { //the current line describes the entities occurrences:
                //todo checks: prefix.equals("o") && currentEntity != null

                String[] occurrences = content.split(";");
                for (String occurrence : occurrences) {
                    String[] occParts = occurrence.split(",");
                    if (occParts.length == 2) {
                        try {
                            int lineNumber = Integer.parseInt(occParts[0].trim());
                            String refTypeStr = occParts[1].trim();
                            NamedEntityReferenceType refType = NamedEntityReferenceType.fromPrefix(refTypeStr);
                            if (refType != null) { //todo sonst fehler
                                currentEntity.addSentenceOccurrence(lineNumber, refType);
                            }
                        } catch (NumberFormatException ignored) {
                            //TOdo hier und an allen anderen stellen loggen wenn iwas failed oder komisch ist mit warn oder so?
                        }
                    } else {
                        //fehler!todo
                    }
                }

                currentlyInEntityLine = true; //get ready for next line
                currentEntity = null;
            }
        }

        return result;
    }

    private void addAlternativeName(String alternativeName) {
        if (alternativeName == null || alternativeName.trim().isEmpty()) {
            return;
        }
        if (alternativeNames == null) {
            alternativeNames = new HashSet<>();
        }
        alternativeNames.add(alternativeName);
    }

    private void addSentenceOccurrence(int sentenceNumber, NamedEntityReferenceType referenceType) {
        if (sentenceNumber < 1) {
            return;
        }
        if (sentenceOccurrences == null) {
            sentenceOccurrences = new HashSet<>();
        }
        sentenceOccurrences.add(new Pair<>(sentenceNumber, referenceType));
    }

    @Override
    public String toString() {
        return "NamedEntity{" + "name='" + name + '\'' + ", type=" + type + ", alternativeNames=" + alternativeNames + ", sentenceOccurrences=" + sentenceOccurrences + '}';
    }

    private enum NamedEntityReferenceType {
        DIRECT("d"), CO_REFERENCE("c");

        private final String prefix;

        NamedEntityReferenceType(String prefix) {
            this.prefix = prefix;
        }

        public static NamedEntityReferenceType fromPrefix(String prefix) {
            for (NamedEntityReferenceType type : NamedEntityReferenceType.values()) {
                if (type.prefix.equals(prefix)) return type;
            }
            //todo logging
            return null;
        }
    }


}
