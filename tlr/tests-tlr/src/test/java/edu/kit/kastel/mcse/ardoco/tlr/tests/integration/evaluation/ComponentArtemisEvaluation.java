package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.io.File;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.execution.AbstractArtemis;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ComponentArtemis;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;

public class ComponentArtemisEvaluation extends AbstractArtemisEvaluation {

    public ComponentArtemisEvaluation(ArtemisEvaluationProject project, LargeLanguageModel llmForNer) {
        super(project, llmForNer);
    }

    public AbstractArtemis createArtemis() {
        String projectName = project.getName();
        ModelFormat architectureModelFormat = ModelFormat.PCM;
        ArchitectureConfiguration architectureModel = new ArchitectureConfiguration(
                project.getTlrTask().getEvaluationProject().getArchitectureModel(architectureModelFormat), architectureModelFormat);
        File documentationFile = project.getTlrTask().getEvaluationProject().getTextFile();
        File outputDirectory = new File("target", projectName + "-output");
        outputDirectory.mkdirs();

        var artemis = new ComponentArtemis(projectName);

        artemis.setUp(documentationFile, architectureModel, null, SortedMaps.immutable.empty(), outputDirectory, llmForNer);
        return artemis;
    }

    @Override
    public MutableSortedSet<String> getTraceLinksAsStrings(ImmutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks) {
        return traceLinks.collect(tl -> tl.getFirstEndpoint().getSentenceNumber() + " -> " + tl.getSecondEndpoint().getId()).toSortedSet();
    }
}
