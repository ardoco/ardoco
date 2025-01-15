/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.core.api.entity;

public non-sealed class TextEntity extends Entity {
    private static final long serialVersionUID = 7693834560590279832L;

    protected TextEntity(String name) {
        super(name);
    }

    protected TextEntity(String name, String id) {
        super(name, id);
    }

}
