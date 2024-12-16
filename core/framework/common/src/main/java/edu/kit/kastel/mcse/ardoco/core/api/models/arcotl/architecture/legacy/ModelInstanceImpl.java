/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy;

import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;

/**
 * This class represents an instance extracted from a model. The name of an instance (as well as the type) are splitted at spaces and can be seen as multiple
 * names. Therefore, the longestName (and type) is the original name (type) of the instance.
 */
@Deprecated
public final class ModelInstanceImpl extends ModelInstance {

    private static final long serialVersionUID = 9185325416212743266L;

    private final String fullName;
    private final String fullType;
    private final MutableList<String> names;
    private final MutableList<String> types;
    private final String uid;

    /**
     * Creates a new instance.
     *
     * @param name name of the instance.
     * @param type type of the instance.
     * @param uid  unique identifier of the instance needed for trace linking.
     */
    public ModelInstanceImpl(String name, String type, String uid) {
        super(name, uid);

        String splitName = CommonUtilities.splitCases(name);
        this.names = Lists.mutable.with(splitName.split(" "));
        if (this.names.size() > 1) {
            this.names.add(name);
        }

        String splitType = CommonUtilities.splitCases(type);
        this.types = Lists.mutable.with(splitType.split(" "));
        if (this.types.size() > 1) {
            this.types.add(type);
        }
        this.uid = uid;
        this.fullName = name;
        this.fullType = type;
    }

    @Override
    public String getType() {
        try {
            throw new IllegalAccessException("This method was just added for refactoring");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the longest name of the instance.
     *
     * @return the original name of the instance
     */
    @Override
    public String getFullName() {
        return this.fullName;
    }

    /**
     * Returns the longest type of the instance.
     *
     * @return the original type of the instance
     */
    @Override
    public String getFullType() {
        return this.fullType;
    }

    /**
     * Returns all name parts of the instance.
     *
     * @return all name parts of the instance as list
     */
    @Override
    public ImmutableList<String> getNameParts() {
        return this.names.toImmutable();
    }

    /**
     * Returns all type parts of the instance.
     *
     * @return all type parts of the instance as list
     */
    @Override
    public ImmutableList<String> getTypeParts() {
        return this.types.toImmutable();
    }

    /**
     * Returns the unique identifier of the instance.
     *
     * @return the unique identifier of the instance
     */
    @Override
    public String getUid() {
        return this.uid;
    }

    @Override
    public String toString() {
        return "Instance [names=" + String.join(", ", this.names) + ", type=" + String.join(", ", this.types) + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fullName, this.fullType, this.uid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModelInstanceImpl other)) {
            return false;
        }
        return Objects.equals(this.fullName, other.fullName) && Objects.equals(this.fullType, other.fullType) && Objects.equals(this.uid, other.uid);
    }

}
