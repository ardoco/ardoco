/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

public enum CodeModelType implements ModelType {
    CODE_MODEL;

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.CODE;
    }
}
