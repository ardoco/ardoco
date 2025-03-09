package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.extraction.cpp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppElementExtractor;


public class CppVariableExtractorTest {
    private final String sourcePath = "src/test/resources/cpp/interface/edu/";

    @Test
    void variableExtractorMainCPPTest() throws IOException {
        String filePath = sourcePath + "src/main.cpp";
        List<VariableElement> variables = extractVariablesFromFile(filePath);

        Assertions.assertEquals(2, variables.size());
        Assertions.assertEquals("myCar", variables.get(0).getName());
        Assertions.assertEquals("Entities::Car", variables.get(0).getDataType());
        Assertions.assertEquals("main()", variables.get(0).getParent().getName());
        Assertions.assertEquals(Type.FUNCTION, variables.get(0).getParent().getType());
        Assertions.assertEquals("person", variables.get(1).getName());
        Assertions.assertEquals("Entities::Person", variables.get(1).getDataType());
        Assertions.assertEquals("main()", variables.get(1).getParent().getName());
        Assertions.assertEquals(Type.FUNCTION, variables.get(1).getParent().getType());
    }

    @Test
    void variableExtractorEntitiesTest() throws IOException {
        String filePath = sourcePath + "src/Entities.cpp";
        List<VariableElement> variables = extractVariablesFromFile(filePath);

        Assertions.assertEquals(0, variables.size());
    }

    @Test
    void variableExtractorEntitiesHTest() throws IOException {
        String filePath = sourcePath + "include/Entities.h";
        List<VariableElement> variables = extractVariablesFromFile(filePath);

        Assertions.assertEquals(7, variables.size());
        Assertions.assertEquals("make", variables.get(0).getName());
        Assertions.assertEquals("std::string", variables.get(0).getDataType());
        Assertions.assertEquals("Car", variables.get(0).getParent().getName());
        Assertions.assertEquals(Type.CLASS, variables.get(0).getParent().getType());
        Assertions.assertEquals("model", variables.get(1).getName());
        Assertions.assertEquals("std::string", variables.get(1).getDataType());
        Assertions.assertEquals("Car", variables.get(1).getParent().getName());
        Assertions.assertEquals(Type.CLASS, variables.get(1).getParent().getType());
        Assertions.assertEquals("year", variables.get(2).getName());
        Assertions.assertEquals("int", variables.get(2).getDataType());
        Assertions.assertEquals("Car", variables.get(2).getParent().getName());
        Assertions.assertEquals(Type.CLASS, variables.get(2).getParent().getType());
        Assertions.assertEquals("name", variables.get(3).getName());
        Assertions.assertEquals("std::string", variables.get(3).getDataType());
        Assertions.assertEquals("Person", variables.get(3).getParent().getName());
        Assertions.assertEquals(Type.CLASS, variables.get(3).getParent().getType());
        Assertions.assertEquals("age", variables.get(4).getName());
        Assertions.assertEquals("int", variables.get(4).getDataType());
        Assertions.assertEquals("Person", variables.get(4).getParent().getName());
        Assertions.assertEquals(Type.CLASS, variables.get(4).getParent().getType());
        Assertions.assertEquals("*ownedCar", variables.get(5).getName());
        Assertions.assertEquals("Car", variables.get(5).getDataType());
        Assertions.assertEquals("Person", variables.get(5).getParent().getName());
        Assertions.assertEquals(Type.CLASS, variables.get(5).getParent().getType());
        Assertions.assertEquals("mechanicName", variables.get(6).getName());
        Assertions.assertEquals("std::string", variables.get(6).getDataType());
        Assertions.assertEquals("Mechanic", variables.get(6).getParent().getName());
        Assertions.assertEquals(Type.CLASS, variables.get(6).getParent().getType());
    }

    private List<VariableElement> extractVariablesFromFile(String filePath) throws IOException {
        CppElementExtractor extractor = new CppElementExtractor();
        Path path = Path.of(filePath);
        extractor.extract(path);
        return extractor.getElements().getVariables();
    }

}
