package edu.kit.kastel.mcse.ardoco.secdreqan;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class Requirement {

    private String uid;

    private String text;

    private String origin_id;

    private SecurityObjective objective;

    private String author;

    private String confidence;

    private Set<Entity> entityLabels;

    private Set<String> comments;

    public String getUID() {
        return uid;
    }

    public String getText() {
        return text;
    }

    public Requirement(@JsonProperty("id") String uid, @JsonProperty("text") String text, @JsonProperty("origin_id") String origin_id,
            @JsonProperty("security_objective") SecurityObjective objective, @JsonProperty("author") String author,
            @JsonProperty("security_confidence") String confidence, @JsonProperty("label") List<List<Object>> labelObjects,
            @JsonProperty("Comments") List<String> comments) {
        this.uid = uid;
        this.text = text;
        this.origin_id = origin_id;
        this.objective = objective;
        this.author = author;
        this.confidence = confidence;

        this.entityLabels = new HashSet<>();

        if (labelObjects != null) {
            for (List<Object> labelObject : labelObjects) {
                int from = (int) labelObject.get(0);
                int to = (int) labelObject.get(1);
                String label = labelObject.get(2).toString();

                var entityLabel = new Entity(from, to, text.substring(from, to), label);
                entityLabels.add(entityLabel);
            }
        }

        if (comments != null) {
            this.comments = new HashSet<>(comments);
        } else {
            this.comments = new HashSet<>();
        }
    }

    public void addComments(List<String> comments) {
        this.comments.addAll(comments);
    }

    public void addEntityLabels(List<Entity> entityLabels) {
        this.entityLabels.addAll(entityLabels);
    }

    public List<String> getComments() {
        return this.comments.stream().toList();
    }

    public List<Entity> getEntities() {
        return this.entityLabels.stream().toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Requirement that = (Requirement) o;
        return Objects.equals(uid, that.uid) && Objects.equals(getText(), that.getText()) && Objects.equals(origin_id,
                that.origin_id) && objective == that.objective && Objects.equals(author, that.author) && Objects.equals(confidence, that.confidence);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(uid);
        result = 31 * result + Objects.hashCode(getText());
        result = 31 * result + Objects.hashCode(origin_id);
        result = 31 * result + Objects.hashCode(objective);
        result = 31 * result + Objects.hashCode(author);
        result = 31 * result + Objects.hashCode(confidence);
        return result;
    }
}
