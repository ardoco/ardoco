package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ClassArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ComponentArtemisNerStrategy;

public enum ArtemisInconsistencyApproach {
    COMPONENT("Component", true, true, true) {
        @Override
        public List<ArtemisNerStrategy> createStrategies() {
            return List.of(new ComponentArtemisNerStrategy());
        }

        @Override
        public List<ArtemisNerStrategy> createStrategiesWithoutModelElement(String excludedModelElementId) {
            return List.of(new HoldBackComponentArtemisNerStrategy(excludedModelElementId));
        }
    },

    CLASS("Class", false, true, false) {//TODO supportsholdback wird hier gerade ignoriert...

        @Override
        public List<ArtemisNerStrategy> createStrategies() {
            return List.of(new ClassArtemisNerStrategy());
        }
    };

    private final String displayName;
    private final boolean supportsMeat;
    private final boolean supportsTeam;
    private final boolean supportsHoldBack;

    ArtemisInconsistencyApproach(String displayName, boolean supportsMeat, boolean supportsTeam, boolean supportsHoldBack) {
        this.displayName = displayName;
        this.supportsMeat = supportsMeat;
        this.supportsTeam = supportsTeam;
        this.supportsHoldBack = supportsHoldBack;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean supportsMeat() {
        return supportsMeat;
    }

    public boolean supportsTeam() {
        return supportsTeam;
    }

    public boolean supportsHoldBack() {
        return supportsHoldBack;
    }

    public abstract List<ArtemisNerStrategy> createStrategies();

    public List<ArtemisNerStrategy> createStrategiesWithoutModelElement(String excludedModelElementId) {
        throw new UnsupportedOperationException(this + " does not support hold-back runs");
    }

}
