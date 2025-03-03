package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.mappers;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java.JavaExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaCommentMapper;

public class JavaCommentMapperTest {
    private final String sourcePath = "src/test/resources/interface/edu/";

    @Test
    void testJavaCommentMapperAClass() throws IOException {
        String filePath = sourcePath + "AClass.java";
        JavaCommentMapper commentMapper = testJavaCommentMapper(filePath);
        // Assertions
        Assertions.assertEquals("s", commentMapper.getVariables().get(0).getName());
        Assertions.assertEquals("This is a Test Line Comment", commentMapper.getVariables().get(0).getComment());

        Assertions.assertEquals("AClass", commentMapper.getClasses().get(0).getName());
        Assertions.assertEquals("This is a Test Java Doc Comment", commentMapper.getClasses().get(0).getComment());
        Assertions.assertEquals("aMethod", commentMapper.getControls().get(0).getName());
        Assertions.assertEquals("This is a Test Block Comment", commentMapper.getControls().get(0).getComment());
    }

    @Test
    void testJavaCommentMapperSuperclass() throws IOException {
        String filePath = sourcePath + "Superclass.java";
        JavaCommentMapper commentMapper = testJavaCommentMapper(filePath);

        // Assertions
        Assertions.assertEquals("Superclass", commentMapper.getClasses().get(0).getName());
        Assertions.assertEquals("This is a Test Java Doc Comment over multiple lines",
                commentMapper.getClasses().get(0).getComment());
    }

    private JavaCommentMapper testJavaCommentMapper(String path) throws IOException {
        CodeItemRepository repository = new CodeItemRepository();
        JavaExtractor extractor = new JavaExtractor(repository, path);
        extractor.extractElements();

        JavaCommentMapper commentMapper = new JavaCommentMapper(extractor.getVariables(), extractor.getControls(),
                extractor.getClasses(), extractor.getInterfaces(), extractor.getComments());
        commentMapper.mapComments();
        return commentMapper;
    }

}
