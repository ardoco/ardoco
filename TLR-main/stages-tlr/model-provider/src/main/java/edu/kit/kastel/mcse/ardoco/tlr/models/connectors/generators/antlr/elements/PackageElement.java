package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class PackageElement extends BasicElement {
    private String shortName;

    public PackageElement(String name, String path) {
        super(name, path);
        this.shortName = name;
    }

    public String[] getPackageNameParts(String regex) {
        return this.getName().split(regex);
    }

    public void updateShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public boolean extendsPackage(PackageElement packageElement) {
        return this.getName().startsWith(packageElement.getName()) && !this.equals(packageElement);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PackageElement) {
            PackageElement packageElement = (PackageElement) obj;
            return packageElement.getName().equals(this.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

}
