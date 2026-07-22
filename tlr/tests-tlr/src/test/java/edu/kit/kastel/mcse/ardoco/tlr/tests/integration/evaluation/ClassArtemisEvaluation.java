package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.io.File;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.execution.AbstractArtemis;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ClassArtemis;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;

public class ClassArtemisEvaluation extends AbstractArtemisEvaluation {

    public ClassArtemisEvaluation(ArtemisEvaluationProject project, LargeLanguageModel llmForNer) {
        super(project, llmForNer);
    }

    public AbstractArtemis createArtemis() {
        String projectName = project.getName();

        CodeConfiguration codeModel = new CodeConfiguration(project.getTlrTask().getEvaluationProject().getCodeModelFromResources(),
                CodeConfiguration.CodeConfigurationType.ACM_FILE);
        File documentationFile = project.getTlrTask().getEvaluationProject().getTextFile();
        File outputDirectory = new File("target", projectName + "-output");
        outputDirectory.mkdirs();

        var artemis = new ClassArtemis(projectName);

        artemis.setUp(documentationFile, null, codeModel, SortedMaps.immutable.empty(), outputDirectory, llmForNer);
        return artemis;
    }

    @Override
    public MutableSortedSet<String> getTraceLinksAsStrings(ImmutableList<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks) {
        return traceLinks.collect(tl -> tl.getFirstEndpoint().getSentenceNumber() + " -> " + tl.getSecondEndpoint()).toSortedSet();
    }
}
