/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture;

public final class ArchitectureMethod extends ArchitectureItem {

    private static final long serialVersionUID = 6560555992331464264L;
    private String type;

    public ArchitectureMethod(String name, String type) {
        super(name, type);
    }

    @Override
    public String getType() {
        return this.type;
    }

}
