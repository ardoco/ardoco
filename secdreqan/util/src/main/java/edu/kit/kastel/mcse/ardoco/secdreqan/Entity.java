package edu.kit.kastel.mcse.ardoco.secdreqan;

import java.util.Objects;

public class Entity {

    private final EntityLabel label;

    private final int from;

    private final String sequence;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Entity entity = (Entity) o;
        return from == entity.from && to == entity.to && label == entity.label;
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, from, to);
    }

    private final int to;

    public Entity(int from, int to, String label, String sequence) {
        this.sequence = sequence;
        this.label = EntityLabel.fromString(label);
        this.from = from;
        this.to = to;
    }

    public String getSequence() {
        return this.sequence;
    }
}
