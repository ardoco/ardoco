/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

public class ModelEntityNode {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    private String modelId;
    private String metamodel;
}
