/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.id.types;

import java.util.Locale;
import java.util.Objects;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ModelInconsistency;

public class MissingTextForModelElementInconsistency implements ModelInconsistency {
    private static final String INCONSISTENCY_TYPE_NAME = "MissingTextForModelElement";

    private final ModelInstance instance;

    public MissingTextForModelElementInconsistency(ModelInstance instance) {
        this.instance = instance;
    }

    @Override
    public String getReason() {
        return String.format(Locale.US, "Model contains an Instance \"%s\" (type: \"%s\")  that seems to be undocumented.", instance.getFullName(), instance
                .getFullType());
    }

    @Override
    public String getType() {
        return INCONSISTENCY_TYPE_NAME;
    }

    @Override
    public String toString() {
        return "MissingTextForModelElementInconsistency [modelInstance=" + instance + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MissingTextForModelElementInconsistency other)) {
            return false;
        }
        return Objects.equals(instance, other.instance);
    }

    @Override
    public ImmutableCollection<String[]> toFileOutput() {
        String[] entry = { getType(), instance.getFullName(), instance.getFullType() };
        var list = Lists.mutable.<String[]>empty();
        list.add(entry);
        return list.toImmutable();
    }

    @Override
    public String getModelInstanceName() {
        return instance.getFullName();
    }

    @Override
    public String getModelInstanceType() {
        return instance.getFullType();
    }

    @Override
    public String getModelInstanceUid() {
        return instance.getUid();
    }
}
