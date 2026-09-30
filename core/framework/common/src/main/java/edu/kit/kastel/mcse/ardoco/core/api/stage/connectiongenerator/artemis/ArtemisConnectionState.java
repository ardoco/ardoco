/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;

@Deterministic
public interface ArtemisConnectionState extends IConfigurable {

    boolean addNamedEntities(MutableList<NamedArchitectureEntity> namedEntities);

    boolean addTraceLinks(MutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks);

    boolean addUnlinkedNamedEntities(MutableList<NamedArchitectureEntity> namedEntities);

    ImmutableList<NamedArchitectureEntity> getNamedEntities();

    ImmutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> getTraceLinks();

    ImmutableList<NamedArchitectureEntity> getUnlinkedNamedEntities();
}
