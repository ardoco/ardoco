package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.cpp;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.cpp.CppVariableExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import generated.antlr.cpp.CPP14Lexer;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14Parser.TranslationUnitContext;

public class CppVariableExtractorTest {
    private final String sourcePath = "src/test/resources/cpp/interface/edu/";

    @Test
    void variableExtractorMainCPPTest() throws IOException {
        String filePath = sourcePath + "src/main.cpp";
        List<VariableElement> variables = extractVariablesFromFile(filePath);

        Assertions.assertEquals(2, variables.size());
        Assertions.assertEquals("myCar", variables.get(0).getName());
        Assertions.assertEquals("Entities::Car", variables.get(0).getType());
        Assertions.assertEquals("person", variables.get(1).getName());
        Assertions.assertEquals("Entities::Person", variables.get(1).getType());
    }

    @Test
    void variableExtractorEntitiesTest() throws IOException {
        String filePath = sourcePath + "src/Entities.cpp";
        List<VariableElement> variables = extractVariablesFromFile(filePath);

        Assertions.assertEquals(0, variables.size());
    }

    @Test
    void variableExtractorEntitiesHTest() throws IOException{
        String filePath = sourcePath + "include/Entities.h";
        List<VariableElement> variables = extractVariablesFromFile(filePath);

        Assertions.assertEquals(7, variables.size());
        Assertions.assertEquals("make", variables.get(0).getName());
        Assertions.assertEquals("std::string", variables.get(0).getType());
        Assertions.assertEquals("model", variables.get(1).getName());
        Assertions.assertEquals("std::string", variables.get(1).getType());
        Assertions.assertEquals("year", variables.get(2).getName());
        Assertions.assertEquals("int", variables.get(2).getType());
        Assertions.assertEquals("name", variables.get(3).getName());
        Assertions.assertEquals("std::string", variables.get(3).getType());
        Assertions.assertEquals("age", variables.get(4).getName());
        Assertions.assertEquals("int", variables.get(4).getType());
        Assertions.assertEquals("ownedCar", variables.get(5).getName());
        Assertions.assertEquals("Car", variables.get(5).getType());
        Assertions.assertEquals("mechanicName", variables.get(6).getName());
        Assertions.assertEquals("std::string", variables.get(6).getType());
    }

    private List<VariableElement> extractVariablesFromFile(String filePath) throws IOException {
        CharStream charStream = CharStreams.fromFileName(filePath);
        CPP14Lexer lexer = new CPP14Lexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(tokens);
        TranslationUnitContext translationUnit = parser.translationUnit();
        CppVariableExtractor extractor = new CppVariableExtractor();
        return extractor.visitTranslationUnit(translationUnit);
    }

    
}
