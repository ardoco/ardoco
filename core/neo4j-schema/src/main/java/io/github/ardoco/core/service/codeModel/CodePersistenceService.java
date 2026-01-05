package io.github.ardoco.core.service.codeModel;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.*;

import io.github.ardoco.core.entities.codeModel.*;
import io.github.ardoco.core.repository.codeModel.CodeModelRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CodePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(CodePersistenceService.class);


    private final CodeModelRepository repository;
    private final CodeModelMapper mapper;

    public CodePersistenceService(CodeModelRepository repository, CodeModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public void saveCodeModel(CodeModel model) {
        CodeModelNode modelNode = new CodeModelNode(model.getId(), model.getMetamodel().name());
        Map<String, CodeItemNode> cache = new HashMap<>();

        for (CodeItem item : model.getContent()) {
            CodeItemNode itemNode = mapToNode(item, cache);
            modelNode.addContent(itemNode);
        }
        repository.save(modelNode);
    }

    @Transactional(readOnly = true)
    public CodeModel loadCodeModel(String modelId) {
        CodeModelNode node = repository.findByModelId(modelId)
                .orElseThrow(() -> new RuntimeException("CodeModel not found: " + modelId));
        logger.info("Finished loading CodeModel");
        return mapper.mapToDomain(node);
    }

    private CodeItemNode mapToNode(CodeItem item, Map<String, CodeItemNode> cache) {
        if (cache.containsKey(item.getId())) return cache.get(item.getId());

        CodeItemNode node = createNode(item);
        cache.put(item.getId(), node);

        // Map Content (Children)
        for (CodeItem child : item.getContent()) {
            node.addContent(mapToNode(child, cache));
        }

        // Map Datatype Relations
        if (item instanceof Datatype dt && node instanceof DatatypeNode dtNode) {
            for (Datatype ext : dt.getExtendedTypes()) {
                dtNode.addExtendedType((DatatypeNode) mapToNode(ext, cache));
            }
            for (Datatype impl : dt.getImplementedTypes()) {
                dtNode.addImplementedType((DatatypeNode) mapToNode(impl, cache));
            }
        }

        return node;
    }

    private CodeItemNode createNode(CodeItem item) {
        if (item instanceof CodePackage p) {
            return new CodePackageNode(p.getName(), p.getId());
        } else if (item instanceof CodeCompilationUnit c) {
            return new CodeCompilationUnitNode(c.getName(), c.getId(), c.getExtension(),
                    c.getLanguage().name(), c.getPathElements());
        } else if (item instanceof ClassUnit c) {
            return new ClassUnitNode(c.getName(), c.getId());
        } else if (item instanceof InterfaceUnit i) {
            return new InterfaceUnitNode(i.getName(), i.getId());
        } else if (item instanceof CodeAssembly a) {
            return new CodeAssemblyNode(a.getName(), a.getId(), a.getLanguage());
        } else if (item instanceof ControlElement c) {
            return new ControlElementNode(c.getName(), c.getId());
        }
        throw new IllegalArgumentException("Unsupported CodeItem: " + item.getClass().getSimpleName());
    }
}
