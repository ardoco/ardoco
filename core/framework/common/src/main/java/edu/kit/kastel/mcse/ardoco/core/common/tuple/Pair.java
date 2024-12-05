/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.tuple;

import java.io.Serializable;
import java.util.Objects;

public record Pair<T extends Serializable, U extends Serializable>(T first, U second) implements Serializable {

    public boolean hasElement(Serializable element) {
        return Objects.equals(this.first, element) || Objects.equals(this.second, element);
    }

    public Serializable getOtherElement(Serializable element) {
        if (Objects.equals(this.first, element)) {
            return this.second;
        }
        if (Objects.equals(this.second, element)) {
            return this.first;
        }
        throw new IllegalArgumentException("Unknown element: " + element);
    }

}
