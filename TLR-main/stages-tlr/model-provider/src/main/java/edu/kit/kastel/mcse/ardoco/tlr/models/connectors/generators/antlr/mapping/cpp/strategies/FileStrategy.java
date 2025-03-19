package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.strategies;

import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.CppCodeItemBuilder;

public class FileStrategy extends AbstractCppCodeItemStrategy {

    public FileStrategy(CodeItemRepository codeItemRepository, CppCodeItemBuilder cppCodeItemBuilder, CppElementStorageRegistry elementManager) {
        super(codeItemRepository, cppCodeItemBuilder, elementManager);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.FILE);
        return buildFileCodeAssembly(comparable);
    }

    @Override
    public boolean supports(Element element) {
        return this.elementManager.isFileElement(element);
    }

    private CodeItem buildFileCodeAssembly(ElementIdentifier parent) {
        Element file = this.elementManager.getFile(parent);
        SortedSet<CodeItem> content = buildContent(parent);
        CodeAssembly codeAssembly = new CodeAssembly(this.codeItemRepository, file.getName(), content);
        return codeAssembly;
    }    
}
