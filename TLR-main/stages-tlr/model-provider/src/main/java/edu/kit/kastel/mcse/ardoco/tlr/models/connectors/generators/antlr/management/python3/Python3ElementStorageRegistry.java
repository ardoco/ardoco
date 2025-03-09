package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3;

import java.util.EnumMap;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementStorage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementStorageRegistry;

public class Python3ElementStorageRegistry extends ElementStorageRegistry {

    public Python3ElementStorageRegistry() {
        super(createTypeMap());

    }

    private static EnumMap<Type, Class<?>> createTypeMap() {
        EnumMap<Type, Class<?>> typeOfClass = new EnumMap<>(Type.class);
        typeOfClass.put(Type.VARIABLE, VariableElement.class);
        typeOfClass.put(Type.FUNCTION, Element.class);
        typeOfClass.put(Type.CLASS, ClassElement.class);
        typeOfClass.put(Type.MODULE, Element.class);
        typeOfClass.put(Type.PACKAGE, PackageElement.class);
        return typeOfClass;
    }

    protected void registerStorage() {
        registerStorage(Type.VARIABLE, new ElementStorage<VariableElement>(VariableElement.class));
        registerStorage(Type.FUNCTION, new ElementStorage<Element>(Element.class));
        registerStorage(Type.CLASS, new ElementStorage<ClassElement>(ClassElement.class));
        registerStorage(Type.MODULE, new ElementStorage<Element>(Element.class));
        registerStorage(Type.PACKAGE, new ElementStorage<PackageElement>(PackageElement.class));
    }

}
