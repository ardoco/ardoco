package io.github.ardoco.core.neo4jschema.service.codeModel;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnitsAndPackages;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.*;

import io.github.ardoco.core.neo4jschema.entities.codeModel.*;
import io.github.ardoco.core.neo4jschema.repository.codeModel.CodeModelRepository;

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

    @Transactional(readOnly = true)
    public SortedSet<Metamodel> getStoredCodeModelMetamodels() {
        SortedSet<Metamodel> available = new java.util.TreeSet<>();
        List<CodeModelNode> archNodes = repository.findAll();
        for (CodeModelNode node : archNodes) {
            try {
                available.add(Metamodel.valueOf(node.getMetamodel()));
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown metamodel found in stored Code Models: " + node.getMetamodel());
            }
        }
        return available;
    }

    @Transactional(readOnly = true)
    public CodeModel loadCodeModel(Metamodel metamodel) {

        List<CodeModelNode> nodes = repository.findAll();
        for (CodeModelNode node : nodes) {
            if (node.getMetamodel().equals(metamodel.name())) {
                return mapper.mapToDomain(node);
            }
        }
        logger.warn("No Code Model found for type: " + metamodel);
        return null;
    }

    @Transactional
    public void saveCodeModel(CodeModel model) {
        logger.info("saving code model");
        CodeModelNode modelNode = new CodeModelNode(model.getId(), model.getMetamodel().name());
        Map<String, CodeItemNode> cache = new HashMap<>();

        CodeModel.CodeModelDto dto = model.createCodeModelDto();
        List<String> contentIds = dto.content();
        CodeItemRepository itemRepository = dto.codeItemRepository();

        List<CodeItem> contentItems = itemRepository.getCodeItemsByIds(contentIds);
        Set<String> modelContentIdSet = new HashSet<>(contentIds);

        if (model instanceof CodeModelWithCompilationUnitsAndPackages) {
            for (CodeItem item : contentItems) {
                if (isRootInModel(item, modelContentIdSet)) {
                    modelNode.addContent(mapToNode(item, cache));
                }
            }
        } else if (model instanceof CodeModelWithCompilationUnits) {
            for (CodeItem item : contentItems) {
                modelNode.addContent(mapToNode(item, cache));
            }
        } else {
            for (CodeItem item : contentItems) {
                modelNode.addContent(mapToNode(item, cache));
            }
        }
        logger.info("about to save code model");
        repository.save(modelNode);
    }

    /**
     * Checks if the given code item is a "Root" relative to the set of IDs in the model.
     * An item is a root if it has no parent, or if its parent is not part of the modelContentIdSet.
     */
    private boolean isRootInModel(CodeItem item, Set<String> modelContentIdSet) {
        if (item instanceof CodeModule cm) {
            if (cm.hasParent() && modelContentIdSet.contains(cm.getParent().getId())) {
                return false;
            }
        }

        else if (item instanceof Datatype dt) {
            if (dt.getCompilationUnit() != null && modelContentIdSet.contains(dt.getCompilationUnit().getId())) {
                return false;
            }
            if (dt.getParentDatatype() != null && modelContentIdSet.contains(dt.getParentDatatype().getId())) {
                return false;
            }
        }
        return true;
    }

    private CodeItemNode mapToNode(CodeItem item, Map<String, CodeItemNode> cache) {
        if (cache.containsKey(item.getId())) return cache.get(item.getId());

        CodeItemNode node = createNode(item);
        cache.put(item.getId(), node);

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
