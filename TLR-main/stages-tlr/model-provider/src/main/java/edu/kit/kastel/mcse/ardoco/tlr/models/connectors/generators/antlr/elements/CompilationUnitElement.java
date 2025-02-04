package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;


public class CompilationUnitElement extends BasicElement {
    private final String pathString;
    private final PackageElement packageElement;

    public CompilationUnitElement(String name, String pathString, String packageName) {
        super(name);
        this.pathString = pathString;
        this.packageElement = new PackageElement(packageName);
    }

    public String getPathString() {
        return pathString;
    }

    public PackageElement getPackage() {
        return packageElement;
    }







    
}
