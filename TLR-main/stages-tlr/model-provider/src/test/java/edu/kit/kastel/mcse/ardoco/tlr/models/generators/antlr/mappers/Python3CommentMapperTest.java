package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.mappers;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3.Python3Extractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.Python3CommentMapper;

public class Python3CommentMapperTest {
    private final String sourcePath = "src/test/resources/python/interface/edu/";

    @Test
    void testPython3CommentMapperAClass() throws IOException {
        String filePath = sourcePath + "APyClass.py";
        Python3CommentMapper commentMapper = testPython3CommentMapper(filePath);
        // Assertions
        Assertions.assertEquals("AClass", commentMapper.getClasses().get(0).getName());
        Assertions.assertEquals("This is a Comment for AClass", commentMapper.getClasses().get(0).getComment());
        
        Assertions.assertEquals("class_variable", commentMapper.getVariables().get(0).getName());
        Assertions.assertEquals("This is an inline comment for class_variable of AClass", commentMapper.getVariables().get(0).getComment());
        
        Assertions.assertEquals("InnerClass1", commentMapper.getClasses().get(1).getName());
        Assertions.assertEquals("This is a multiple line comment for InnerClass1", commentMapper.getClasses().get(1).getComment());

        Assertions.assertEquals("InnerClass2", commentMapper.getClasses().get(2).getName());
        Assertions.assertEquals("This is a multiple line comment for InnerClass2", commentMapper.getClasses().get(2).getComment());
    }



    private Python3CommentMapper testPython3CommentMapper(String path) throws IOException {
        CodeItemRepository repository = new CodeItemRepository();
        Python3Extractor extractor = new Python3Extractor(repository, path);
        extractor.extractElements();

        Python3CommentMapper commentMapper = new Python3CommentMapper(extractor.getVariables(), extractor.getControls(), extractor.getClasses(), extractor.getComments());
        commentMapper.mapComments();
        return commentMapper;
    }
    
}
