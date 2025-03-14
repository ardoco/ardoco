package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.strategies;

import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3.Python3ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CodeItemBuilder;

public class PackageStrategy extends AbstractPython3CodeItemStrategy {

    public PackageStrategy(CodeItemRepository repository, CodeItemBuilder codeItemBuilder, Python3ElementManager elementManager) {
        super(repository, codeItemBuilder, elementManager);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.PACKAGE);
        return buildCodePackage(comparable);
    }

    @Override
    public boolean supports(Element element) {
        return elementManager.isPackageElement(element);
    }

    private CodePackage buildCodePackage(ElementIdentifier parent) {
        PackageElement packageElement = elementManager.getPackage(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        CodePackage codePackage = new CodePackage(codeItemRepository, packageElement.getShortName(), content);
        codePackage.setComment(packageElement.getComment());
        return codePackage;
    }
    
}
