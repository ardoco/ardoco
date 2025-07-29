package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.NerConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

public class NerConnectionGenerator extends AbstractExecutionStage {

    public NerConnectionGenerator(DataRepository dataRepository) {
        // TODO
        super(List.of(), NerConnectionGenerator.class.getSimpleName(), dataRepository);
    }

    public static NerConnectionGenerator get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var connectionGenerator = new NerConnectionGenerator(dataRepository);
        connectionGenerator.applyConfiguration(additionalConfigs);
        return connectionGenerator;
    }

    @Override
    protected void initializeState() {
        var activeMetamodels = this.getDataRepository().getData(ModelStates.ID, ModelStates.class).orElseThrow().getMetamodels();
        var connectionStates = NerConnectionStatesImpl.build(activeMetamodels.toArray(Metamodel[]::new));
        getDataRepository().addData(NerConnectionStates.ID, connectionStates);
    }
}
