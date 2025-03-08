package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;

public abstract class ModelMapper {
    public final ProgrammingLanguage language;
    protected final CodeItemRepository codeItemRepository;
    private CodeModel codeModel;

    protected ModelMapper(CodeItemRepository codeItemRepository, ProgrammingLanguage language) {
        this.codeItemRepository = codeItemRepository;
        this.language = language;
    }

    public CodeModel getCodeModel() {
        return codeModel;
    }
    
    public void mapToCodeModel() {
        List<Parent> rootParents = getRootParents();
        SortedSet<CodeItem> content = buildContentFromRoot(rootParents);
        codeModel = new CodeModel(codeItemRepository, content);
    }

    protected SortedSet<CodeItem> buildContentFromRoot(List<Parent> parents) {
        SortedSet<CodeItem> content = new TreeSet<>();

        for (Parent parent : parents) {
            content.add(buildSubtree(parent));
        }
        return content;
    }

    protected SortedSet<CodeItem> buildContent(Parent parent) {
        List<BasicElement> items = getElementsWithParent(parent);
        SortedSet<CodeItem> content = new TreeSet<>();
        for (BasicElement item : items) {
            CodeItem codeItem = buildCodeItem(item);
            if (codeItem != null) {
                content.add(codeItem);
            }
        }
        return content;
    }


    protected abstract List<Parent> getRootParents();
    protected abstract CodeItem buildSubtree(Parent parent);
    protected abstract List<BasicElement> getElementsWithParent(Parent parent);
    protected abstract CodeItem buildCodeItem(BasicElement element);
}
