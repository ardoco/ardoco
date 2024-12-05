/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction;

/**
 * @deprecated should be replaced to make mappings serializable
 */
@Deprecated
public interface NounMappingChangeListener {
    void onDelete(NounMapping deletedNounMapping, NounMapping replacement);
}
