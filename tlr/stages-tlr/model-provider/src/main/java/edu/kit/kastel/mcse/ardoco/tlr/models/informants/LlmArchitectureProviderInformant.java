/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import static edu.kit.kastel.mcse.ardoco.tlr.models.informants.LlmArchitecturePrompt.Features.PACKAGES;
import static edu.kit.kastel.mcse.ardoco.tlr.models.informants.LlmArchitecturePrompt.Features.PACKAGES_AND_THEIR_CLASSES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureComponentModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.measures.levenshtein.LevenshteinMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class LlmArchitectureProviderInformant extends Informant {
    private static final String MODEL_STATES_DATA = "ModelStatesData";

    private static final Logger logger = LoggerFactory.getLogger(LlmArchitectureProviderInformant.class);

    private final ChatModel chatLanguageModel;
    private final LlmArchitecturePrompt documentationPrompt;
    private final LlmArchitecturePrompt codePrompt;
    private final LlmArchitecturePrompt aggregationPrompt;
    private final LlmArchitecturePrompt.Features codeFeature;

    public LlmArchitectureProviderInformant(DataRepository dataRepository, LargeLanguageModel largeLanguageModel, LlmArchitecturePrompt documentation,
            LlmArchitecturePrompt code, LlmArchitecturePrompt.Features codeFeature, LlmArchitecturePrompt aggregation) {
        super(LlmArchitectureProviderInformant.class.getSimpleName(), dataRepository);
        String apiKey = Environment.getEnv("OPENAI_API_KEY");
        String orgId = Environment.getEnv("OPENAI_ORGANIZATION_ID");
        if ((apiKey == null || orgId == null) && largeLanguageModel != null && largeLanguageModel.isOpenAi()) {
            throw new IllegalArgumentException("OpenAI API Key and Organization ID must be set");
        }
        this.chatLanguageModel = largeLanguageModel == null ? null : largeLanguageModel.create();
        this.documentationPrompt = documentation;
        this.codePrompt = code;
        this.codeFeature = codeFeature;
        this.aggregationPrompt = aggregation;
        if (largeLanguageModel != null && documentationPrompt == null && codePrompt == null) {
            throw new IllegalArgumentException("At least one prompt must be provided");
        }
        if (documentationPrompt != null && codePrompt != null && aggregationPrompt == null) {
            logger.info("Using Similarity Metrics to aggregate the component names");
        }
        if (codePrompt != null && codeFeature == null) {
            throw new IllegalArgumentException("Code prompt requires a code feature");
        }
    }

    @Override
    protected void process() {
        List<String> componentNamesDocumentation = new ArrayList<>();
        List<String> componentNamesCode = new ArrayList<>();
        if (documentationPrompt != null)
            documentationToArchitecture(componentNamesDocumentation);
        if (codePrompt != null)
            codeToArchitecture(componentNamesCode);

        List<String> componentNames = new ArrayList<>();

        if (aggregationPrompt != null) {
            var allComponentNames = Stream.concat(componentNamesDocumentation.stream(), componentNamesCode.stream()).toList();
            var aggregation = chatLanguageModel.chat(aggregationPrompt.getTemplates().getFirst().formatted(String.join("\n", allComponentNames)));
            logger.info("Response (Aggregation): {}", aggregation);
            parseComponentNames(aggregation, componentNames);
        } else if (documentationPrompt != null && codePrompt != null) {
            componentNames = mergeViaSimilarity(componentNamesDocumentation, componentNamesCode);
        } else {
            // If only one prompt is provided, use the component names from that prompt
            componentNames = Stream.concat(componentNamesDocumentation.stream(), componentNamesCode.stream()).toList();
        }

        componentNames = componentNames.stream()
                .map(it -> it.replace("Components", "").replace("Component", "").trim())
                .map(this::toCamelCase)
                .filter(it -> !it.isBlank())
                .distinct()
                .sorted()
                .toList();

        if (logger.isInfoEnabled()) {
            logger.info("Component names:\n{}", String.join("\n", componentNames));
        }
        buildModel(componentNames);
    }

    private String toCamelCase(String name) {
        if (!name.contains(" ")) {
            return name;
        }
        String[] parts = name.split(" ");
        StringBuilder camelCaseName = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                camelCaseName.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) {
                    camelCaseName.append(part.substring(1).toLowerCase());
                }
            }
        }
        return camelCaseName.toString();
    }

    private List<String> mergeViaSimilarity(List<String> componentNamesDocumentation, List<String> componentNamesCode) {
        WordSimUtils simUtils = new WordSimUtils();
        simUtils.setMeasures(Collections.singletonList(new LevenshteinMeasure(CommonTextToolsConfig.LEVENSHTEIN_MIN_LENGTH,
                CommonTextToolsConfig.LEVENSHTEIN_MAX_DISTANCE, 0.5)));
        List<String> componentNames = new ArrayList<>();
        for (String componentName : Stream.concat(componentNamesDocumentation.stream(), componentNamesCode.stream()).toList()) {
            if (componentNames.stream().noneMatch(it -> simUtils.areWordsSimilar(it, componentName))) {
                componentNames.add(componentName);
            } else {
                logger.info("Similar component name found (skipping): {}", componentName);
            }
        }
        return componentNames;
    }

    private void documentationToArchitecture(List<String> componentNames) {
        var inputText = DataRepositoryHelper.getInputText(dataRepository);
        parseComponentsFromAiRequests(componentNames, documentationPrompt.getTemplates(), inputText);
    }

    private void codeToArchitecture(List<String> componentNames) {
        var models = DataRepositoryHelper.getModelStatesData(dataRepository);
        CodeModel codeModel = (CodeModel) models.getModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES);
        if (codeModel == null) {
            logger.warn("Code model not found");
            return;
        }

        switch (this.codeFeature) {
            case PACKAGES -> {
                var packages = codeModel.getAllPackages().stream().filter(it -> !it.getContent().isEmpty()).toList();
                parseComponentsFromAiRequests(componentNames, codePrompt.getTemplates(PACKAGES), String.join("\n", packages.stream()
                        .map(this::getPackageName)
                        .toList()));
            }
            case PACKAGES_AND_THEIR_CLASSES -> {
                var packages = codeModel.getAllPackages().stream().filter(it -> !it.getContent().isEmpty()).toList();

                var packagesWithClasses = packages.stream().map(p -> {
                    var packageName = getPackageName(p);
                    var classes = p.getContent().stream().flatMap(it -> it.getAllCompilationUnits().stream()).map(Entity::getName).distinct().sorted().toList();
                    return packageName + " (" + String.join(", ", classes) + ")";
                }).toList();

                parseComponentsFromAiRequests(componentNames, codePrompt.getTemplates(PACKAGES_AND_THEIR_CLASSES), String.join("\n", packagesWithClasses));
            }
        }

    }

    private void parseComponentsFromAiRequests(List<String> componentNames, List<String> templates, String dataForFirstPrompt) {
        String startMessage = templates.getFirst().formatted(dataForFirstPrompt);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(UserMessage.from(startMessage));

        var initialResponse = chatLanguageModel.chat(messages).aiMessage();
        messages.add(initialResponse);
        logger.info("Initial Response: {}", initialResponse.text());

        for (String nextMessage : templates.stream().skip(1).toList()) {
            messages.add(UserMessage.from(nextMessage));
            var response = chatLanguageModel.chat(messages).aiMessage();
            logger.info("Response: {}", response.text());
            messages.add(response);
        }

        parseComponentNames(((AiMessage) messages.getLast()).text(), componentNames);
    }

    private String getPackageName(CodePackage codePackage) {
        List<String> packageName = new ArrayList<>();
        packageName.add(codePackage.getName());
        var parent = codePackage.getParent();
        while (parent != null) {
            packageName.add(parent.getName());
            parent = parent.getParent();
        }
        packageName = packageName.reversed();
        return String.join(".", packageName);
    }

    private void parseComponentNames(String response, List<String> componentNames) {
        for (String line : response.split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            line = line.trim();

            if (line.startsWith("-")) {
                // Defined Format "- Name1"
                var name = line.substring(1).trim();
                componentNames.add(name);
            } else {
                logger.warn("Could not parse component name: {}", line);
            }
        }
    }

    private void buildModel(List<String> componentNames) {
        List<ArchitectureItem> componentList = componentNames.stream()
                .map(it -> new ArchitectureComponent(it, it, new TreeSet<>(), new TreeSet<>(), new TreeSet<>(), "Component"))
                .collect(Collectors.toList());
        ArchitectureModel am = new ArchitectureComponentModel(new ArchitectureModelWithComponentsAndInterfaces(componentList));
        Optional<ModelStates> modelStatesOptional = dataRepository.getData(MODEL_STATES_DATA, ModelStates.class);
        var modelStates = modelStatesOptional.orElseGet(ModelStates::new);

        modelStates.addModel(am.getMetamodel(), am);

        if (modelStatesOptional.isEmpty()) {
            dataRepository.addData(MODEL_STATES_DATA, modelStates);
        }
    }
}
