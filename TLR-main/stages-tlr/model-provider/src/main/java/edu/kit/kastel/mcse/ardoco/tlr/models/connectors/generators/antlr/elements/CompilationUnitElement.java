package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;


public class CompilationUnitElement extends BasicElement {
    private final String pathString;
    private final String packageName;

    public CompilationUnitElement(String name, String pathString, String packageName) {
        super(name);
        this.pathString = pathString;
        this.packageName = packageName;
    }

    public String getPathString() {
        return pathString;
    }

    public String getPackageName() {
        return packageName;
    }





    
}
