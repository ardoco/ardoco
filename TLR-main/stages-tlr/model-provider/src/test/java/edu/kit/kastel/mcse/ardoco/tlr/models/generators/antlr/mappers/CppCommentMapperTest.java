package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.mappers;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.cpp.CppExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mappers.CppCommentMapper;

public class CppCommentMapperTest {
    private final String sourcePath = "src/test/resources/cpp/interface/edu/";

    @Test
    void testCppCommentMapperMainCPP() throws IOException {
        String filePath = sourcePath + "src/main.cpp";
        CppCommentMapper commentMapper = buildCppCommentMapper(filePath);

        // Assertions
        Assertions.assertEquals("main()", commentMapper.getControls().get(0).getName());
        Assertions.assertEquals("Simple C++ Project Author: Your Name Description: A basic C++ project with a simple structure.\nmain.cpp", commentMapper.getControls().get(0).getComment());
    }    

    @Test
    void testCppCommentMapperEntitiesCPP() throws IOException {
        String filePath = sourcePath + "src/Entities.cpp";
        CppCommentMapper commentMapper = buildCppCommentMapper(filePath);

        // Assertions
        Assertions.assertEquals("Entities", commentMapper.getNamespaces().get(0).getName());
        Assertions.assertEquals("Entities.cpp", commentMapper.getNamespaces().get(0).getComment());
    }

    @Test
    void testCppCommentMapperEntitiesH() throws IOException {
        String filePath = sourcePath + "include/Entities.h";
        CppCommentMapper commentMapper = buildCppCommentMapper(filePath);

        // Assertions
        Assertions.assertEquals("Entities", commentMapper.getNamespaces().get(0).getName());
        Assertions.assertEquals("Entities.h", commentMapper.getNamespaces().get(0).getComment());
    }

    private CppCommentMapper buildCppCommentMapper(String path) throws IOException {
        CodeItemRepository repository = new CodeItemRepository();
        CppExtractor extractor = new CppExtractor(repository, path);
        extractor.execute();

        CppCommentMapper commentMapper = new CppCommentMapper(extractor.getVariables(), extractor.getControls(), extractor.getClasses(), extractor.getNamespaces(), extractor.getComments());
        commentMapper.mapComments();
        return commentMapper;
    }
}
