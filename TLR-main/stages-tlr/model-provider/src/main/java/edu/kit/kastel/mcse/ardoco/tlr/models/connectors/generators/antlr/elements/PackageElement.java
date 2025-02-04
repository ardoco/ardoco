package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class PackageElement extends BasicElement {
    private String shortName;

    public PackageElement(String name) {
        super(name);
        this.shortName = name;
    }

    public String[] getPackageNameParts() {
        return this.getName().split(".");
    }

    public void updateShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PackageElement) {
            PackageElement packageElement = (PackageElement) obj;
            return packageElement.getName().equals(this.getName());
        }
        return false;
    }

    public boolean extendsPackage(PackageElement packageElement) {
        return this.getName().startsWith(packageElement.getName()) && !this.equals(packageElement);
    }
    
}
