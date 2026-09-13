/* Licensed under MIT 2022-2026. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import edu.kit.kastel.mcse.ardoco.core.api.InputTextData;
import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.SimplePreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.text.SimpleText;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.ProjectPipelineData;

/**
 * Utility class to help access and manipulate {@link DataRepository} and its stored pipeline data.
 */
public final class DataRepositoryHelper {

    private DataRepositoryHelper() {
    }

    /**
     * Checks whether there is {@link ProjectPipelineData} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link ProjectPipelineData} within the {@link DataRepository}; else, false
     */
    public static boolean hasProjectPipelineData(DataRepository dataRepository) {
        return dataRepository.getData(ProjectPipelineData.ID, ProjectPipelineData.class).isPresent();
    }

    /**
     * Returns the {@link ProjectPipelineData} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and
     * throw an {@link java.util.NoSuchElementException} if the data is not present. To make sure that there is data present, use
     * {@link #hasProjectPipelineData(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the data
     */
    public static ProjectPipelineData getProjectPipelineData(DataRepository dataRepository) {
        return dataRepository.getData(ProjectPipelineData.ID, ProjectPipelineData.class).orElseThrow();
    }

    /**
     * Returns the input text as String stored within the provided {@link DataRepository}.
     * If missing in-memory and Neo4j is available, loads from Project metadata (sole-store / resume).
     *
     * @param dataRepository the DataRepository to access
     * @return the text
     */
    public static String getInputText(DataRepository dataRepository) {
        var inMemory = dataRepository.getData(InputTextData.ID, InputTextData.class);
        if (inMemory.isPresent()) {
            return inMemory.get().getText();
        }
        if (PersistenceBridge.isAvailable() && hasProjectPipelineData(dataRepository)) {
            String projectName = getProjectPipelineData(dataRepository).getProjectName();
            String loaded = PersistenceBridge.callQuietly("loadProjectInputText",
                    () -> PersistenceBridge.getHandler().loadProjectInputText(projectName), null);
            if (loaded != null) {
                dataRepository.addData(InputTextData.ID, new InputTextData(loaded));
                return loaded;
            }
        }
        throw new java.util.NoSuchElementException("No InputTextData in DataRepository or Neo4j");
    }

    /**
     * Put the given input text into the given {@link DataRepository}. This will override existing data!
     * Dual-writes to Neo4j Project metadata when persistence is available and project name is known.
     *
     * @param dataRepository the dataRepository
     * @param inputText      the input inputText
     */
    public static void putInputText(DataRepository dataRepository, String inputText) {
        dataRepository.addData(InputTextData.ID, new InputTextData(inputText));
        if (PersistenceBridge.isAvailable() && hasProjectPipelineData(dataRepository)) {
            String projectName = getProjectPipelineData(dataRepository).getProjectName();
            PersistenceBridge.runQuietly("saveProjectMetadata",
                    () -> PersistenceBridge.getHandler().saveProjectMetadata(projectName, inputText));
        }
    }

    /**
     * Checks whether there is annotated {@link Text} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link Text} within the {@link DataRepository}; else, false
     */
    public static boolean hasAnnotatedText(DataRepository dataRepository) {
        if (dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).isPresent()) {
            return true;
        }
        if (PersistenceBridge.isAvailable()) {
            Boolean fromNeo4j = PersistenceBridge.callQuietly("hasPreprocessedText",
                    () -> PersistenceBridge.getHandler().hasPreprocessedText(PreprocessingData.ID), null);
            if (fromNeo4j != null) {
                return fromNeo4j;
            }
        }
        return false;
    }

    /**
     * Returns the {@link Text} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and throw an
     * {@link java.util.NoSuchElementException} if the data is not present. To make sure that there is data present, use
     * {@link #hasAnnotatedText(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the text
     */
    public static Text getAnnotatedText(DataRepository dataRepository) {
        var inMemory = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class);
        if (inMemory.isPresent()) {
            return inMemory.get().getText();
        }
        if (PersistenceBridge.isAvailable()) {
            Text loaded = PersistenceBridge.callQuietly("loadPreprocessedText",
                    () -> PersistenceBridge.getHandler().loadPreprocessedText(PreprocessingData.ID), null);
            if (loaded != null) {
                return loaded;
            }
        }
        throw new java.util.NoSuchElementException("No annotated text in DataRepository or Neo4j");
    }

    /**
     * Checks whether there is annotated {@link SimpleText} stored within the provided {@link DataRepository}
     * or (when persistence is available) in Neo4j.
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link SimpleText} within the {@link DataRepository}; else, false
     */
    public static boolean hasSimpleText(DataRepository dataRepository) {
        if (dataRepository.getData(SimplePreprocessingData.ID, SimplePreprocessingData.class).isPresent()) {
            return true;
        }
        if (PersistenceBridge.isAvailable()) {
            Boolean fromNeo4j = PersistenceBridge.callQuietly("hasSimpleText",
                    () -> PersistenceBridge.getHandler().hasSimpleText(SimplePreprocessingData.ID), null);
            if (fromNeo4j != null) {
                return fromNeo4j;
            }
        }
        return false;
    }

    /**
     * Returns the {@link SimpleText} stored within the provided {@link DataRepository}.
     * If missing in-memory and Neo4j is available, loads SimpleText (sole-store / resume).
     *
     * @param dataRepository the DataRepository to access
     * @return the text
     */
    public static SimpleText getSimpleText(DataRepository dataRepository) {
        var inMemory = dataRepository.getData(SimplePreprocessingData.ID, SimplePreprocessingData.class);
        if (inMemory.isPresent()) {
            return inMemory.get().getText();
        }
        if (PersistenceBridge.isAvailable()) {
            SimpleText loaded = PersistenceBridge.callQuietly("loadSimpleText",
                    () -> PersistenceBridge.getHandler().loadSimpleText(SimplePreprocessingData.ID), null);
            if (loaded != null) {
                dataRepository.addData(SimplePreprocessingData.ID, new SimplePreprocessingData(loaded));
                return loaded;
            }
        }
        throw new java.util.NoSuchElementException("No SimplePreprocessingData in DataRepository or Neo4j");
    }

    /**
     * Returns the {@link TextState} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and throw an
     * {@link java.util.NoSuchElementException} if the state is not present.
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static TextState getTextState(DataRepository dataRepository) {
        return dataRepository.getData(TextState.ID, TextState.class).orElseThrow();
    }

    /**
     * Returns the {@link ModelStates} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and throw
     * an {@link java.util.NoSuchElementException} if the state is not present.
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static ModelStates getModelStatesData(DataRepository dataRepository) {
        return dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow();
    }

    /**
     * Checks whether there is {@link RecommendationStates} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link RecommendationStates} within the {@link DataRepository}; else, false
     */
    public static boolean hasRecommendationStates(DataRepository dataRepository) {
        return dataRepository.getData(RecommendationStates.ID, RecommendationStates.class).isPresent();
    }

    /**
     * Returns the {@link RecommendationStates} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail
     * and throw an {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use
     * {@link #hasRecommendationStates(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static RecommendationStates getRecommendationStates(DataRepository dataRepository) {
        return dataRepository.getData(RecommendationStates.ID, RecommendationStates.class).orElseThrow();
    }

    /**
     * Checks whether there is {@link ConnectionStates} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link ConnectionStates} within the {@link DataRepository}; else, false
     */
    public static boolean hasConnectionStates(DataRepository dataRepository) {
        return dataRepository.getData(ConnectionStates.ID, ConnectionStates.class).isPresent();
    }

    /**
     * Returns the {@link ConnectionStates} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and
     * throw an {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use
     * {@link #hasConnectionStates(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static ConnectionStates getConnectionStates(DataRepository dataRepository) {
        return dataRepository.getData(ConnectionStates.ID, ConnectionStates.class).orElseThrow();
    }

    /**
     * Checks whether there is {@link ConnectionStates} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link ConnectionStates} within the {@link DataRepository}; else, false
     */
    public static boolean hasNerConnectionStates(DataRepository dataRepository) {
        return dataRepository.getData(NerConnectionStates.ID, NerConnectionStates.class).isPresent();
    }

    /**
     * Returns the {@link NerConnectionStates} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and
     * throw an {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use
     * {@link #hasConnectionStates(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static NerConnectionStates getNerConnectionStates(DataRepository dataRepository) {
        return dataRepository.getData(NerConnectionStates.ID, NerConnectionStates.class).orElseThrow();
    }

    /**
     * Checks whether there is {@link InconsistencyStates} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link InconsistencyStates} within the {@link DataRepository}; else, false
     */
    public static boolean hasInconsistencyStates(DataRepository dataRepository) {
        return dataRepository.getData(InconsistencyStates.ID, InconsistencyStates.class).isPresent();
    }

    /**
     * Returns the {@link InconsistencyStates} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and
     * throw an {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use
     * {@link #hasInconsistencyStates(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static InconsistencyStates getInconsistencyStates(DataRepository dataRepository) {
        if (hasInconsistencyStates(dataRepository)) {
            return dataRepository.getData(InconsistencyStates.ID, InconsistencyStates.class).orElseThrow();
        }
        return null;
    }

    /**
     * Checks whether there is {@link CodeTraceabilityState} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link CodeTraceabilityState} within the {@link DataRepository}; else, false
     */
    public static boolean hasCodeTraceabilityState(DataRepository dataRepository) {
        return dataRepository.getData(CodeTraceabilityState.ID, CodeTraceabilityState.class).isPresent();
    }

    /**
     * Returns the {@link CodeTraceabilityState} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail
     * and throw an {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use
     * {@link #hasInconsistencyStates(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static CodeTraceabilityState getCodeTraceabilityState(DataRepository dataRepository) {
        if (hasCodeTraceabilityState(dataRepository)) {
            return dataRepository.getData(CodeTraceabilityState.ID, CodeTraceabilityState.class).orElseThrow();
        }
        return null;
    }

    /**
     * Put the given {@link PreprocessingData} into the given {@link DataRepository}. This will override existing data!
     *
     * @param dataRepository    the dataRepository
     * @param preprocessingData the preprocessingData
     */
    public static void putPreprocessingData(DataRepository dataRepository, PreprocessingData preprocessingData) {
        // Keep in-memory state even if Neo4j is down (fault isolation).
        dataRepository.addData(PreprocessingData.ID, preprocessingData);
        if (PersistenceBridge.isAvailable()) {
            PersistenceBridge.runQuietly("savePreprocessedText",
                    () -> PersistenceBridge.getHandler().savePreprocessedText(preprocessingData.getText(), PreprocessingData.ID));
        }
    }

    /**
     * Put the given {@link SimplePreprocessingData} into the given {@link DataRepository}. This will override existing data!
     * Dual-writes SimpleText to Neo4j when persistence is available.
     *
     * @param dataRepository    the dataRepository
     * @param preprocessingData the preprocessingData
     */
    public static void putSimplePreprocessingData(DataRepository dataRepository, SimplePreprocessingData preprocessingData) {
        dataRepository.addData(SimplePreprocessingData.ID, preprocessingData);
        if (PersistenceBridge.isAvailable()) {
            PersistenceBridge.runQuietly("saveSimpleText",
                    () -> PersistenceBridge.getHandler().saveSimpleText(preprocessingData.getText(), SimplePreprocessingData.ID));
        }
    }
}
