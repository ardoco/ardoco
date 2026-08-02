package edu.kit.kastel.mcse.ardoco.tlr.artemis.states;

import java.util.Collection;
import java.util.SortedSet;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface ArtemisTraceabilityState extends PipelineStepData {

    boolean addNamedEntities(Collection<NamedArchitectureEntity> namedEntities);

    boolean addTraceLinks(Collection<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks);

    boolean addUnlinkedNamedEntities(Collection<NamedArchitectureEntity> namedEntities);

    SortedSet<NamedArchitectureEntity> getNamedEntities();

    ImmutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> getTraceLinks();

    SortedSet<NamedArchitectureEntity> getUnlinkedNamedEntities();
}
