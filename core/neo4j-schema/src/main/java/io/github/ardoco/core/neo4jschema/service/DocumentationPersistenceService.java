/* Licensed under MIT 2023-2026. */
package io.github.ardoco.core.neo4jschema.service;

import io.github.ardoco.core.neo4jschema.mapper.DocumentationMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import io.github.ardoco.core.neo4jschema.entities.documentation.TextNode;
import io.github.ardoco.core.neo4jschema.repository.documentation.TextNodeRepository;

import java.util.Optional;

@Service
public class DocumentationPersistenceService {

    private final TextNodeRepository textRepository;
    private final DocumentationMapper documentationMapper;

    public DocumentationPersistenceService(TextNodeRepository textRepository, DocumentationMapper mapper) {
        this.textRepository = textRepository;
        this.documentationMapper = mapper;
    }

    /**
     * Checks if a preprocessed TextNode exists in the Neo4j database for the given identifier. This method performs a simple existence check using the
     * TextNodeRepository, which is optimized for this purpose.
     *
     * @param identifier The identifier to check for the existence of a corresponding TextNode in the database.
     * @return true if a TextNode with the given identifier exists, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean hasPreprocessedText(String identifier) {
        return textRepository.existsByArdocoId(identifier);
    }

    /**
     * Loads a preprocessed Text from the Neo4j database based on the provided identifier. This method performs a deep fetch of the TextNode and its related
     * entities to reconstruct the domain Text object. If no TextNode is found for the given identifier, it returns null.
     *
     * @param identifier The identifier used to locate the TextNode in the database
     * @return The domain Text object reconstructed from the TextNode, or null if no matching TextNode is found.
     */
    @Transactional(readOnly = true)
    public Optional<Text> loadPreprocessedText(String identifier) throws IllegalStateException {
        return textRepository.findByArdocoId(identifier)
                .map(documentationMapper::toDomain);
    }

    /**
     * Saves the provided domain Text object into the Neo4j database. This method first deletes any existing TextNode associated with the given documentId to
     * ensure that stale data is not retained. Then, it converts the domain Text into a TextNode entity using the DocumentationMapper and persists it using the
     * TextNodeRepository.
     *
     * @param domainText The domain Text object to be saved into the database.
     * @param documentId The identifier to associate with the saved TextNode entity, used for future retrieval and deletion operations.
     */

    @Transactional
    public void savePreprocessedText(Text domainText, String documentId) {
        deletePreprocessedText(documentId);
        TextNode textNode = documentationMapper.toNode(domainText, documentId);
        textRepository.save(textNode);
    }

    /**
     * Deletes a preprocessed TextNode and all its associated child nodes from the Neo4j database based on the provided identifier. Moreover, this method also
     * deletes all TraceLinks and Inconsistencies that point to the sentences of the TextNode.
     *
     * @param identifier The identifier used to locate the TextNode in the database for deletion.
     */
    @Transactional
    public void deletePreprocessedText(String identifier) {
        textRepository.deleteByArdocoId(identifier);
    }

}
