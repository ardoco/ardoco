/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.tests.evaluation;

import java.io.File;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.common.RepositoryHandler;

public enum EvaluationProject {
    MEDIASTORE(//
            "MS", //
            "/benchmark/mediastore/model_2016/pcm/ms.repository", //
            "/benchmark/mediastore/text_2016/mediastore.txt", //
            "https://github.com/ArDoCo/MediaStore3.git", //
            "94c398fa02b3d6b8d71517522a7206d37ed3a9af", //
            "/benchmark/mediastore/model_2016/code/codeModel.acm");

    private static final Logger logger = LoggerFactory.getLogger(EvaluationProject.class);

    private final String alias;
    private final String architectureModelResource;
    private final String textResource;
    private final String codeRepository;
    private final String codeCommit;
    private final String codeModelResource;

    EvaluationProject(String alias, String architectureModelResource, String textResource, String codeRepository, String codeCommit, String codeModelResource) {
        this.alias = alias;
        this.architectureModelResource = architectureModelResource;
        this.textResource = textResource;
        this.codeRepository = codeRepository;
        this.codeCommit = codeCommit;
        this.codeModelResource = codeModelResource;
    }

    public String getAlias() {
        return alias;
    }

    public File getArchitectureModel(ModelFormat modelFormat) {
        return switch (modelFormat) {
            case PCM -> EvaluationHelper.loadFileFromResources(architectureModelResource);
            case UML -> EvaluationHelper.loadFileFromResources(architectureModelResource.replace("/pcm/", "/uml/").replace(".repository", ".uml"));
            case ACM, RAW -> throw new IllegalArgumentException("Model format " + modelFormat + " is not supported for this project.");
        };
    }

    public File getTextFile() {
        return EvaluationHelper.loadFileFromResources(textResource);
    }

    /**
     * Get the code model for this project. If the code model is stored in an ACM file, it will be loaded from resources. If not, the code will be cloned from
     * the repository and the folder will be returned.
     * 
     * @param fromStoredAcmFile whether to load the code model from the stored ACM file or to clone the repository
     * @return the acm file if fromStoredAcmFile is true, otherwise the folder containing the code
     */
    public File getCodeModel(boolean fromStoredAcmFile) {
        if (fromStoredAcmFile) {
            return EvaluationHelper.loadFileFromResources(this.codeModelResource);
        }

        File codeLocation = getTemporaryCodeLocation();
        if (!codeLocation.exists() || Objects.requireNonNull(codeLocation.listFiles()).length == 0) {
            RepositoryHandler.shallowCloneRepository(codeRepository, codeLocation.getAbsolutePath(), codeCommit);
        }
        return codeLocation;
    }

    private File getTemporaryCodeLocation() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        var temporary = new File(tmpdir + File.separator + "ArDoCo" + File.separator + this.name());
        logger.debug("Location of Code: {}", temporary.getAbsolutePath());
        temporary.mkdirs();
        return temporary;
    }

}
