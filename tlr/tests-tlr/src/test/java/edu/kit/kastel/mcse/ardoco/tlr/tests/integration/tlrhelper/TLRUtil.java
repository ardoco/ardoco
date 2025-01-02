/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;

/**
 * This utility class provides methods for TLR.
 */
public final class TLRUtil {

    private TLRUtil() {
        throw new IllegalAccessError();
    }

    /**
     * extracts the trace links from a {@link DataRepository}
     *
     * @param data the {@link EvaluationResults}
     * @return the trace links
     */
    public static ImmutableList<ModelElementSentenceLink> getTraceLinks(DataRepository data) {
        var traceLinks = Lists.mutable.<ModelElementSentenceLink>empty();
        var connectionStates = data.getData(ConnectionStates.ID, ConnectionStates.class).orElseThrow();
        var modelStates = data.getData(ModelStates.ID, ModelStates.class).orElseThrow();

        List<ConnectionState> connectionStatesList = modelStates.modelIds()
                .stream()
                .map(modelStates::getModel)
                .map(Model::getMetamodel)
                .map(connectionStates::getConnectionState)
                .toList();
        for (var connectionState : connectionStatesList) {
            traceLinks.addAll(
                    connectionState.getTraceLinks().stream().map(it -> new ModelElementSentenceLink(it.getFirstEndpoint(), it.getSecondEndpoint())).toList());
        }
        return traceLinks.toImmutable();
    }
}
