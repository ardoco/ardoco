/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.codeModel;

import org.springframework.data.neo4j.core.schema.Node;

@Node("CodeAssembly")
public class CodeAssemblyNode extends CodeModuleNode {
    private String language;

    public CodeAssemblyNode(String name, String ardocoId, String language) {
        super(name, ardocoId);
        this.language = language;
    }

    protected CodeAssemblyNode() {
    }

    public String getLanguage() {
        return language;
    }
}
