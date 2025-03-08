package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management.Python3ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.python3.Python3Parser;
import generated.antlr.python3.Python3ParserBaseVisitor;

public class Python3ElementExtractor extends Python3ParserBaseVisitor<Void> {
    private final Python3ElementManager elementManager;

    public Python3ElementExtractor() {
        this.elementManager = new Python3ElementManager();
    }

    public Python3ElementManager getElementManager() {
        return elementManager;
    }

    public void extract(Python3Parser.File_inputContext ctx) {
        visitFile_input(ctx);
        addModules(ctx);
    }

    @Override
    public Void visitFile_input(Python3Parser.File_inputContext ctx) {
        super.visitFile_input(ctx);
        return null;
    }

    @Override
    public Void visitClassdef(Python3Parser.ClassdefContext ctx) {
        super.visitClassdef(ctx);
        String name = ctx.name().getText();
        String path = PathExtractor.extractPath(ctx);
        List<String> childClassOf = getParentClasses(ctx);
        Parent parent = new Python3ParentExtractor().getParent(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addClassElement(name, path, parent, childClassOf, startLine, endLine);
        return null;
    }

    @Override
    public Void visitFuncdef(Python3Parser.FuncdefContext ctx) {
        super.visitFuncdef(ctx);
        String name = ctx.name().getText();
        String path = PathExtractor.extractPath(ctx);
        Parent parent = new Python3ParentExtractor().getParent(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addFunctionElement(name, path, parent, startLine, endLine);
        return null;
    }

    @Override
    public Void visitSimple_stmt(Python3Parser.Simple_stmtContext ctx) {
        if (ctx.expr_stmt() != null) {
            visitExpr_stmt(ctx.expr_stmt());
        } else {
            super.visitSimple_stmt(ctx);
        }
        return null;
    }

    @Override
    public Void visitExpr_stmt(Python3Parser.Expr_stmtContext ctx) {
        if (ctx.ASSIGN() != null && ctx.testlist_star_expr().size() > 1) {
            extractVariablesFromExprStmt(ctx);
        }
        return null;
    }

    private List<String> getParentClasses(Python3Parser.ClassdefContext ctx) {
        List<String> parentClasses = new ArrayList<>();
        if (ctx.arglist() != null) {
            for (Python3Parser.ArgumentContext arg : ctx.arglist().argument()) {
                parentClasses.add(arg.getText());
            }
        }
        return parentClasses;
    }

    private void extractVariablesFromExprStmt(Python3Parser.Expr_stmtContext ctx) {
        List<String> varNames = extract(ctx.testlist_star_expr(0));
        List<String> values = extract(ctx.testlist_star_expr(1));
        List<String> types = inferTypesFromValues(values);
        Parent parent = new Python3ParentExtractor().getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        if (varNames.size() != values.size()) {
            throw new IllegalArgumentException("The number of variable names and values does not match");
        }

        for (int i = 0; i < varNames.size(); i++) {
            addVariableElement(varNames.get(i), path, types.get(i), parent, values.get(i), startLine, endLine);
        }
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
        } else if (value.equals("True") || value.equals("False")) {
            return "bool";
        } else {
            /*
             * Later need to check if it is a Class Object cannot be done here
             * as it requires all classes to be parsed already
             */
            return "any";
        }
    }

    private void addVariableElement(String varName, String path, String type, Parent parent, String value,
            int startLine, int endLine) {
        VariableElement variable = new VariableElement(varName, path, type, parent);
        variable.setStartLine(startLine);
        variable.setEndLine(endLine);
        elementManager.addVariable(variable);
    }

    private void addFunctionElement(String name, String path, Parent parent, int startLine, int endLine) {
        Element function = new Element(name, path, parent);
        function.setStartLine(startLine);
        function.setEndLine(endLine);
        elementManager.addFunction(function);
    }

    private void addClassElement(String name, String path, Parent parent, List<String> childClassOf, int startLine,
            int endLine) {
        ClassElement python3ClassElement = new ClassElement(name, path, parent, childClassOf);
        python3ClassElement.setStartLine(startLine);
        python3ClassElement.setEndLine(endLine);
        elementManager.addClass(python3ClassElement);
    }

    private void addModules(Python3Parser.File_inputContext ctx) {
        String name = PathExtractor.extractNameFromPath(ctx);
        String path = PathExtractor.extractPath(ctx);
        String packagePath = path.substring(0, path.lastIndexOf("/") + 1);
        String packageName = addPackage(packagePath);
        Parent parent = new Parent(packageName, packagePath, Type.PACKAGE);
        Element module = new Element(name, path, parent);
        elementManager.addModule(module);
    }

    private String addPackage(String packagePath) {
        List<PackageElement> packageElements = elementManager.getPackages();
        String closestParentName = "";
        String closestParentPath = "";
        String packageName = "";

        for (PackageElement packageElement : packageElements) {
            if (packageElement.getPath().equals(packagePath)) {
                return packageElement.getName();
            }
            if (packagePath.startsWith(packageElement.getPath())
                    && packageElement.getPath().length() > closestParentPath.length()) {
                closestParentName = packageElement.getName();
                closestParentPath = packageElement.getPath();
            }
        }

        if (!closestParentPath.isEmpty()) {
            Parent parent = new Parent(closestParentName, closestParentPath, Type.PACKAGE);
            packageName = packagePath.substring(closestParentPath.length(), packagePath.length() - 1);
            PackageElement packageElement = new PackageElement(packageName, packagePath, parent);
            elementManager.addPackage(packageElement);
        } else {
            packageName = packagePath.substring(0, packagePath.length() - 1);
            PackageElement packageElement = new PackageElement(packageName, packagePath);
            elementManager.addPackage(packageElement);
        }

        updatePackageParents(packageName, packagePath);

        return packageName;
    }

    private void updatePackageParents(String packageName, String packagePath) {
        List<PackageElement> packageElements = elementManager.getPackages();
        for (PackageElement packageElement : packageElements) {
            if (packageElement.getPath().startsWith(packagePath)
                    && packageElement.getPath().length() > packagePath.length()) {
                Parent parent = new Parent(packageName, packagePath, Type.PACKAGE);
                packageElement.updateParent(parent);
                packageElement.updateShortName(packageElement.getPath().substring(packagePath.length(),
                        packageElement.getPath().length() - 1));
            }
        }
    }

}
