package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class PackageElement extends Element {
    private String shortName;

    public PackageElement(String name, String path) {
        super(name, path);
        this.shortName = name;
    }

    public PackageElement(String name, String path, Parent parent) {
        super(name, path, parent);
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

    public void updateParent(Parent parent) {
        this.setParent(parent);
    }

    public boolean extendsPackage(PackageElement packageElement) {
        return this.getName().startsWith(packageElement.getPath()) && !this.equals(packageElement);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PackageElement) {
            PackageElement packageElement = (PackageElement) obj;
            return packageElement.getPath().equals(this.getPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

}
