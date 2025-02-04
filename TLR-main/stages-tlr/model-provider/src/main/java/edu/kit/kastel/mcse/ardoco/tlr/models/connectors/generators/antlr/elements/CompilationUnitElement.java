package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;


public class CompilationUnitElement extends BasicElement {
    private final PackageElement packageElement;

    public CompilationUnitElement(String name, String path, String packageName) {
        super(name, path);
        this.packageElement = new PackageElement(packageName, path);
    }

    public PackageElement getPackage() {
        return packageElement;
    }







    
}
