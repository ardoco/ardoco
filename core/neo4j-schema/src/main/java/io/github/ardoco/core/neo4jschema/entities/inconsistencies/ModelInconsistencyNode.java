package io.github.ardoco.core.neo4jschema.entities.inconsistencies;

import org.springframework.data.neo4j.core.schema.Node;

import java.util.Objects;

@Node("ModelInconsistencyNode")
public class ModelInconsistencyNode extends InconsistencyNode {

    private static final String INCONSISTENCY_TYPE_NAME = "TextEntityAbsentFromModel";


    private String modelArdocoId;

    public ModelInconsistencyNode(String modelArdocoId, String reason) {
        super(reason, INCONSISTENCY_TYPE_NAME);
        this.modelArdocoId = modelArdocoId;
    }

    public ModelInconsistencyNode() {
    }

    public String getModelArdocoId() {
        return modelArdocoId;
    }

    @Override
    public <T> T accept(InconsistencyNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ModelInconsistencyNode that = (ModelInconsistencyNode) o;
        return Objects.equals(modelArdocoId, that.modelArdocoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), modelArdocoId);
    }
}
