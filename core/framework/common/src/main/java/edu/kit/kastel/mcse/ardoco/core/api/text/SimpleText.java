/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

import java.io.Serializable;

import org.eclipse.collections.api.list.ImmutableList;

public interface SimpleText extends Serializable {

    String getText();

    ImmutableList<String> getLines();

}
