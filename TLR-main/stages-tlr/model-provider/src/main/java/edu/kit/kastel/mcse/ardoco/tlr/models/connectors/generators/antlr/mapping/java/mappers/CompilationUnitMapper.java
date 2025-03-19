package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.mappers;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaCodeItemMapperCollection;

public class CompilationUnitMapper extends AbstractJavaCodeItemMapper {

    public CompilationUnitMapper(CodeItemRepository codeItemRepository, JavaCodeItemMapperCollection javaCodeItemBuilder, JavaElementStorageRegistry elementManager) {
        super(codeItemRepository, javaCodeItemBuilder, elementManager);
    }

    @Override
    public boolean supports(Element element) {
        return this.elementManager.isCompilationUnitElement(element);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.COMPILATIONUNIT);
        return buildCodeCompilationUnit(comparable);
    }

    private CodeCompilationUnit buildCodeCompilationUnit(ElementIdentifier parent) {
        Element compilationUnit = elementManager.getCompilationUnitElement(parent);
        List<String> pathElements = Arrays.asList(compilationUnit.getPath().split("/"));
        SortedSet<CodeItem> content = buildContent(parent);

        PackageElement pack = elementManager.getPackage(compilationUnit.getParentIdentifier());
        CodeCompilationUnit codeCompilationUnit = new CodeCompilationUnit(codeItemRepository, compilationUnit.getName(),
                content, pathElements, pack.getName(), ProgrammingLanguage.JAVA);
        codeCompilationUnit.setComment(compilationUnit.getComment());
        return codeCompilationUnit;
    }
    
}
