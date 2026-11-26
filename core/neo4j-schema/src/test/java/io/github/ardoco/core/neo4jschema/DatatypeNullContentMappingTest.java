/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.Datatype;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.InterfaceUnit;
import io.github.ardoco.core.neo4jschema.entities.codemodel.ClassUnitNode;
import io.github.ardoco.core.neo4jschema.entities.codemodel.CodeItemNode;
import io.github.ardoco.core.neo4jschema.entities.codemodel.CodeModelNode;
import io.github.ardoco.core.neo4jschema.entities.codemodel.InterfaceUnitNode;
import io.github.ardoco.core.neo4jschema.mapper.CodeModelMapper;

/**
 * Checks that a datatype node without stored content ids maps to a domain datatype whose content ids can be read, as an empty list.
 */
class DatatypeNullContentMappingTest {

    @Test
    @DisplayName("An InterfaceUnit node without contentIds maps to an InterfaceUnit with empty content ids")
    void interfaceUnitWithoutContentIdsHasEmptyContentIds() {
        Datatype datatype = mapSingleDatatype(new InterfaceUnitNode("IService", "o4_iface"));

        Assertions.assertInstanceOf(InterfaceUnit.class, datatype);
        List<String> contentIds = Assertions.assertDoesNotThrow(datatype::getContentIds);
        Assertions.assertEquals(List.of(), contentIds);
    }

    @Test
    @DisplayName("A ClassUnit node without contentIds maps to a ClassUnit with empty content ids")
    void classUnitWithoutContentIdsHasEmptyContentIds() {
        Datatype datatype = mapSingleDatatype(new ClassUnitNode("Service", "o4_class"));

        Assertions.assertInstanceOf(ClassUnit.class, datatype);
        List<String> contentIds = Assertions.assertDoesNotThrow(datatype::getContentIds);
        Assertions.assertEquals(List.of(), contentIds);
    }

    private static Datatype mapSingleDatatype(CodeItemNode datatypeNode) {
        Assertions.assertNull(datatypeNode.getContentIds());
        CodeModelNode modelNode = new CodeModelNode("o4_model", Metamodel.CODE_WITH_COMPILATION_UNITS.name());
        modelNode.addContent(datatypeNode);
        modelNode.addRepositoryItem(datatypeNode);

        CodeModel model = Assertions.assertDoesNotThrow(() -> new CodeModelMapper().toDomain(modelNode));

        List<? extends CodeItem> content = model.getContent();
        Assertions.assertEquals(1, content.size());
        return Assertions.assertInstanceOf(Datatype.class, content.getFirst());
    }
}
