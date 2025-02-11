package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;

public class Python3ModuleElement extends BasicElement {
    private final PackageElement packageElement;

    public Python3ModuleElement(String name, String path, String packageName) {
        super(name, path);
        this.packageElement = new PackageElement(packageName, path);
    }

    public PackageElement getPackage() {
        return packageElement;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Python3ModuleElement) {
            Python3ModuleElement module = (Python3ModuleElement) obj;
            return module.getName().equals(this.getName()) &&
                    module.getPath().equals(this.getPath()) &&
                    module.getPackage().equals(this.getPackage());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode() + this.getPath().hashCode() + this.getPackage().hashCode();
    }
    
}
