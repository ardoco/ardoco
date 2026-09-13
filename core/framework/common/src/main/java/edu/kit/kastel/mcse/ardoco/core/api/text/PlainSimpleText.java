/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * Immutable {@link SimpleText} reconstructed from persistence (or any plain text + lines).
 */
public final class PlainSimpleText implements SimpleText {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String text;
    private final List<String> lines;

    public PlainSimpleText(String text, List<String> lines) {
        this.text = text != null ? text : "";
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public ImmutableList<String> getLines() {
        return Lists.immutable.withAll(lines);
    }
}
