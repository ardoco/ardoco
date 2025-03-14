package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements;

public class PackageElement extends Element {
    private static final Type type = Type.PACKAGE;
    private String shortName;

    public PackageElement(String name, String path) {
        super(name, path, type);
        this.shortName = name;
    }

    public PackageElement(String name, String path, ElementIdentifier parentIdentifier) {
        super(name, path, type, parentIdentifier);
        this.shortName = name;
    }

    public String[] getPackageNameParts(String regex) {
        return this.identifier.name().split(regex);
    }

    public void updateShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public void updateParent(ElementIdentifier parent) {
        this.identifierOfParent = parent;
    }

    public boolean extendsPackage(PackageElement packageElement) {
        return this.identifier.path().startsWith(packageElement.identifier.path()) && !this.equals(packageElement);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PackageElement) {
            PackageElement packageElement = (PackageElement) obj;
            return packageElement.identifier.path().equals(this.identifier.path());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

}
