package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3VariableElement;
import generated.antlr.Python3Parser;
import generated.antlr.Python3ParserBaseVisitor;

public class Python3VariableExtractor extends Python3ParserBaseVisitor<List<Python3VariableElement>> {
    private final List<Python3VariableElement> variables = new ArrayList<>();
    private final String fileName;

    public Python3VariableExtractor(String filePath) {
        String potentialFileName = filePath;
        if (potentialFileName.contains("/")) {
            potentialFileName = potentialFileName.substring(potentialFileName.lastIndexOf("/") + 1, potentialFileName.length());
        }
        if (potentialFileName.contains(".")) {
            potentialFileName = potentialFileName.substring(0, potentialFileName.lastIndexOf("."));
        }
        this.fileName = potentialFileName;
    }

    @Override
    public List<Python3VariableElement> visitFile_input(Python3Parser.File_inputContext ctx) {
        super.visitFile_input(ctx);
        return variables;
    }

    @Override
    public List<Python3VariableElement> visitSimple_stmt(Python3Parser.Simple_stmtContext ctx) {
        if (ctx.expr_stmt() != null) {
            visitExpr_stmt(ctx.expr_stmt());
        } else {
            super.visitSimple_stmt(ctx);
        }
        return variables;
    }


    @Override
    public List<Python3VariableElement> visitExpr_stmt(Python3Parser.Expr_stmtContext ctx) {
        // Expression statment with assignment
        if (ctx.ASSIGN() != null && ctx.testlist_star_expr().size() > 1) {
            List<Python3VariableElement> variables = extractVariables(ctx);
            this.variables.addAll(variables);
        }
        return variables;
    }

    private List<Python3VariableElement> extractVariables(Python3Parser.Expr_stmtContext ctx) {
        List<Python3VariableElement> variables = new ArrayList<>();
        List<String> varName = extract(ctx.testlist_star_expr(0));
        List<String> values = extract(ctx.testlist_star_expr(1));
        List<String> types = inferTypesFromValues(values);
        Parent parent = Python3ParentExtractor.getParent(ctx, this.fileName);

        if (varName.size() != values.size()) {
            throw new IllegalArgumentException("The number of variable names and values does not match");
        }

        for (int i = 0; i < varName.size(); i++) {
            Python3VariableElement variable = new Python3VariableElement(varName.get(i), types.get(i), parent, values.get(i));
            variables.add(variable);
        }

        return variables;
    }

    private List<String> extract(Python3Parser.Testlist_star_exprContext variableDeclarators) {
        List<String> variableNames = new ArrayList<>();
        for (Python3Parser.TestContext testCtx : variableDeclarators.test()) {
            String name = testCtx.getText();
            variableNames.add(name);
        }
        return variableNames;
    }

    private List<String> inferTypesFromValues(List<String> values) {
        List<String> types = new ArrayList<>();

        for (String value : values) {
            types.add(inferTypeFromValue(value));
        }
        return types;
    }

    private String inferTypeFromValue(String value) {
        if (value.matches("^-?\\d+$")) {
            return "int";
        } else if (value.matches("^-?\\d+\\.\\d+$")) {
            return "float";
        } else if (value.matches("^\".*\"$")) {
            return "str";
        } else if (value.equals("True")|| value.equals("False")) {
            return "bool";
        } else {
            /*
            Later need to check if it is a Class Object cannot be done here 
            as it requires all classes to be parsed already
             */ 
            return "any";
        }
    }

}
