/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.tuple;

import java.io.Serializable;

// TODO Make T Serializable (Claimant) ??
public record Triple<T, U extends Serializable, V extends Serializable>(T first, U second, V third) implements Serializable {
}
