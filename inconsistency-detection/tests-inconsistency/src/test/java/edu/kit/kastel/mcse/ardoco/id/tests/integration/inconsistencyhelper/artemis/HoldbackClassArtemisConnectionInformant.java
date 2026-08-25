package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis;

import java.util.List;

import org.jspecify.annotations.NonNull;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.Datatype;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.informants.ClassArtemisConnectionInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class HoldbackClassArtemisConnectionInformant extends ClassArtemisConnectionInformant {
    private final List<String> heldBackClassNames;

    public HoldbackClassArtemisConnectionInformant(DataRepository dataRepository, ArtemisNerStrategy strategy, List<String> heldBackClassNames) {
        super(dataRepository, strategy);
        this.heldBackClassNames = heldBackClassNames;
    }

    @Override
    protected @NonNull List<Datatype> getClasses(@NonNull CodeModelWithCompilationUnits codeModel) {
        return super.getClasses(codeModel).stream().filter(clazz -> !heldBackClassNames.contains(clazz.getName())).toList();
    }

}
