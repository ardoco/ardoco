/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.model.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.common.RepositoryHandler;

public enum EvaluationProject {
    MEDIASTORE(//
            "/benchmark/mediastore/model_2016/pcm/ms.repository", //
            "/benchmark/mediastore/text_2016/mediastore.txt", //
            "https://github.com/ArDoCo/MediaStore3.git", //
            "94c398fa02b3d6b8d71517522a7206d37ed3a9af", //
            "/benchmark/mediastore/model_2016/code/codeModel.acm"),//

    TEASTORE(//
            "/benchmark/teastore/model_2020/pcm/teastore.repository", //
            "/benchmark/teastore/text_2020/teastore.txt", //
            "https://github.com/ArDoCo/TeaStore.git", //
            "bdc49020a55cfa97eaabbb25744fefbc2697defa", //
            "/benchmark/teastore/model_2022/code/codeModel.acm"),//

    TEAMMATES(//
            "/benchmark/teammates/model_2021/pcm/teammates.repository", //
            "/benchmark/teammates/text_2021/teammates.txt", //
            "https://github.com/ArDoCo/teammates.git", //
            "b24519a2af9e17b2bc9c025e87e4cf60009c425d", //
            "/benchmark/teammates/model_2023/code/codeModel.acm"),//

    BIGBLUEBUTTON(//
            "/benchmark/bigbluebutton/model_2021/pcm/bbb.repository", //
            "/benchmark/bigbluebutton/text_2021/bigbluebutton.txt", //
            "https://github.com/ArDoCo/bigbluebutton.git", //
            "8fa2507d6c3865a9850004fd6fefd09738e68406", //
            "/benchmark/bigbluebutton/model_2023/code/codeModel.acm"), //

    JABREF(//
            "/benchmark/jabref/model_2021/pcm/jabref.repository", //
            "/benchmark/jabref/text_2021/jabref.txt", //
            "https://github.com/ArDoCo/jabref.git", //
            "6269698cae437610ec79c38e6dd611eef7e88afe", //
            "/benchmark/jabref/model_2023/code/codeModel.acm");

    private static final Logger logger = LoggerFactory.getLogger(EvaluationProject.class);

    private final String architectureModelResource;
    private final String textResource;
    private final String codeRepository;
    private final String codeCommit;
    private final String codeModelResource;

    EvaluationProject(String architectureModelResource, String textResource, String codeRepository, String codeCommit, String codeModelResource) {
        this.architectureModelResource = architectureModelResource;
        this.textResource = textResource;
        this.codeRepository = codeRepository;
        this.codeCommit = codeCommit;
        this.codeModelResource = codeModelResource;
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
     * Get the code model for this project. The code will be cloned from
     * the repository and the folder will be returned.
     *
     * @return the folder containing the code
     * @see #getCodeDirectoryWithoutCloning()
     */
    public File getCodeDirectory() {
        File codeLocation = getCodeDirectoryWithoutCloning();
        if (!codeLocation.exists() || Objects.requireNonNull(codeLocation.listFiles()).length == 0) {
            RepositoryHandler.shallowCloneRepository(codeRepository, codeLocation.getAbsolutePath(), codeCommit);
        }
        return codeLocation;
    }

    /**
     * Get the code model for this project. The ACM file will be loaded from resources
     *
     * @return the acm file
     */
    public File getCodeModelFromResources() {
        return EvaluationHelper.loadFileFromResources(this.codeModelResource);
    }

    /**
     * Get the location where the code for this project will be stored.
     * 
     * @return the location of the code
     * @see #getCodeDirectory()
     */
    public File getCodeDirectoryWithoutCloning() {
        String temp = System.getProperty("java.io.tmpdir");
        var temporary = new File(temp + File.separator + "ArDoCo" + File.separator + this.name());
        logger.debug("Location of Code: {}", temporary.getAbsolutePath());
        temporary.mkdirs();
        return temporary;
    }

}
