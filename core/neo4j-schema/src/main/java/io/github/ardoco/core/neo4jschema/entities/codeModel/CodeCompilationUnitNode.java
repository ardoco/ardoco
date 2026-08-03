/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.codeModel;

import java.util.List;

import org.springframework.data.neo4j.core.schema.Node;

@Node("CodeCompilationUnit")
public class CodeCompilationUnitNode extends CodeItemNode {

    private String extension; // File extension
    private String language; // Enum stored as String
    private List<String> pathElements;

    public CodeCompilationUnitNode(String name, String ardocoId, String extension, String language, List<String> pathElements) {
        super(name, ardocoId);
        this.extension = extension;
        this.language = language;
        this.pathElements = pathElements;
    }

    protected CodeCompilationUnitNode() {
    }

    public String getExtension() {
        return extension;
    }

    public String getLanguage() {
        return language;
    }

    public List<String> getPathElements() {
        return pathElements;
    }
}
