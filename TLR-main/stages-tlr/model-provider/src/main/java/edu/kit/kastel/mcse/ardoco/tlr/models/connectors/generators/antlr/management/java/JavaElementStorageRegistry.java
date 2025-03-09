package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java;

import java.util.EnumMap;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementStorage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementStorageRegistry;

public class JavaElementStorageRegistry extends ElementStorageRegistry {

    public JavaElementStorageRegistry() {
        super(createTypeMap());
    }

    private static EnumMap<Type, Class<?>> createTypeMap() {
        EnumMap<Type, Class<?>> typeOfClass = new EnumMap<>(Type.class);
        typeOfClass.put(Type.VARIABLE, VariableElement.class);
        typeOfClass.put(Type.FUNCTION, Element.class);
        typeOfClass.put(Type.CLASS, JavaClassElement.class);
        typeOfClass.put(Type.INTERFACE, Element.class);
        typeOfClass.put(Type.COMPILATIONUNIT, Element.class);
        typeOfClass.put(Type.PACKAGE, PackageElement.class);
        return typeOfClass;
    }

    protected void registerStorage() {
        registerStorage(Type.VARIABLE, new ElementStorage<VariableElement>(VariableElement.class));
        registerStorage(Type.FUNCTION, new ElementStorage<Element>(Element.class));
        registerStorage(Type.CLASS, new ElementStorage<JavaClassElement>(JavaClassElement.class));
        registerStorage(Type.INTERFACE, new ElementStorage<Element>(Element.class));
        registerStorage(Type.COMPILATIONUNIT, new ElementStorage<Element>(Element.class));
        registerStorage(Type.PACKAGE, new ElementStorage<PackageElement>(PackageElement.class));
    }
    
}
