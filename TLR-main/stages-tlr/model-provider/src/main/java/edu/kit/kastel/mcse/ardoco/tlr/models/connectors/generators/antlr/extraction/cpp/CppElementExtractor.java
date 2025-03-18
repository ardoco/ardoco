package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ElementExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import generated.antlr.cpp.CPP14Lexer;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14Parser.FunctionBodyContext;
import generated.antlr.cpp.CPP14Parser.TranslationUnitContext;

public class CppElementExtractor extends ElementExtractor {
    private final CppElementManager elementManager;

    public CppElementExtractor() {
        super();
        this.elementManager = new CppElementManager();
        this.commentExtractor = new CppCommentExtractor(elementManager);
    }

    public CppElementExtractor(CppElementManager elementManager) {
        super();
        this.elementManager = elementManager;
        this.commentExtractor = new CppCommentExtractor(elementManager);
    }

    @Override 
    protected CommonTokenStream buildTokens(Path file) throws IOException {
        CharStream stream = CharStreams.fromPath(file);
        CPP14Lexer lexer = new CPP14Lexer(stream);
        return new CommonTokenStream(lexer);
    }

    @Override
    public CppElementManager getElements() {
        return elementManager;
    }

    @Override
    protected List<Path> getFiles(String directoryPath) {
        Path dir = Path.of(directoryPath);
        List<Path> cppFiles = new ArrayList<>();
        try {
            Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".cpp") || f.toString().endsWith(".h"))
                    .forEach(cppFiles::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cppFiles;
    }

    @Override
    public void extractElements(CommonTokenStream tokens) {
        TranslationUnitContext ctx = buildContext(tokens);

        visitTranslationUnit(ctx);
        addFile(ctx);
    }

    private TranslationUnitContext buildContext(CommonTokenStream tokenStream) {
        CPP14Parser parser = new CPP14Parser(tokenStream);
        return parser.translationUnit();
    }

    public Void visitTranslationUnit(CPP14Parser.TranslationUnitContext ctx) {
        if (ctx.declarationseq() != null) {
            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration, null);
            }
        }
        return null;
    }

    public Void visitDeclaration(CPP14Parser.DeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (parentIdentifier == null) {
            parentIdentifier = new ElementIdentifier(PathExtractor.extractNameFromPath(ctx), PathExtractor.extractPath(ctx), Type.FILE);
        }
        if (ctx.functionDefinition() != null) {
            visitFunctionDefinition(ctx.functionDefinition(), parentIdentifier);
        }
        if (ctx.namespaceDefinition() != null) {
            visitNamespaceDefinition(ctx.namespaceDefinition(), parentIdentifier);
        }
        if (ctx.blockDeclaration() != null) {
            visitBlockDeclaration(ctx.blockDeclaration(), parentIdentifier);
        }

        return null;
    }

    public Void visitNamespaceDefinition(CPP14Parser.NamespaceDefinitionContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.declarationseq() != null) {
            ElementIdentifier nameSpaceIdentifier = extractNamespace(ctx, parentIdentifier);

            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration, nameSpaceIdentifier);
            }
        }
        return null;
    }

    public Void visitBlockDeclaration(CPP14Parser.BlockDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.simpleDeclaration() != null) {
            visitSimpleDeclaration(ctx.simpleDeclaration(), parentIdentifier);
        }
        return null;
    }

    public Void visitFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.declarator() == null) {
            return null;
        }
        String name = ctx.declarator().getText();
        ElementIdentifier parent = parentIdentifier;
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier functionIdentifier = new ElementIdentifier(name, path, Type.FUNCTION);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();
        addFunction(functionIdentifier, parent, startLine, endLine);

        if (ctx.functionBody() != null) {
            visitFunctionBody(ctx.functionBody(), functionIdentifier);
        }
        return null;
    }

    public Void visitFunctionBody(FunctionBodyContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.compoundStatement() != null && ctx.compoundStatement().statementSeq() != null
                && ctx.compoundStatement().statementSeq().statement() != null) {
            for (CPP14Parser.StatementContext statement : ctx.compoundStatement().statementSeq().statement()) {
                if (statement.declarationStatement() != null) {
                    visitBlockDeclaration(statement.declarationStatement().blockDeclaration(), parentIdentifier);
                }
            }
        }
        return null;
    }

    public Void visitSimpleDeclaration(CPP14Parser.SimpleDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.declSpecifierSeq() == null) {
            return null;
        }

        for (CPP14Parser.DeclSpecifierContext declSeq : ctx.declSpecifierSeq().declSpecifier()) {
            if (declSeq == null || declSeq.typeSpecifier() == null) {
                continue;
            }
            if (declSeq != null && declSeq.typeSpecifier().classSpecifier() != null) {
                visitClassSpecifier(declSeq.typeSpecifier().classSpecifier(), parentIdentifier);
            }
            // extractVariablesFromClass(declSeq.typeSpecifier().classSpecifier());
        }

        if (ctx.initDeclaratorList() != null) {
            extractVariableElement(ctx, parentIdentifier);
        }
        return null;
    }

    public Void visitClassSpecifier(CPP14Parser.ClassSpecifierContext ctx, ElementIdentifier parentIdentifier) {
        ElementIdentifier classIdentifier = extractClass(ctx, parentIdentifier);
        extractVariablesFromClass(ctx, classIdentifier);
        return null;
    }

    private ElementIdentifier extractNamespace(CPP14Parser.NamespaceDefinitionContext ctx, ElementIdentifier parentIdentifier) {
        String name = "anonymous";
        if (ctx.Identifier() != null) {
            name = ctx.Identifier().getText();
        }
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier parent = parentIdentifier;
        ElementIdentifier namespaceIdentifier = new ElementIdentifier(name, path, Type.NAMESPACE);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();
        addNamespace(namespaceIdentifier, parent, startLine, endLine);
        return namespaceIdentifier;
    }

    private ElementIdentifier extractClass(CPP14Parser.ClassSpecifierContext ctx, ElementIdentifier parentIdentifier) {
        ElementIdentifier identifier = getClassIdentifier(ctx);
        ElementIdentifier parent = parentIdentifier;
        List<String> inherits = getInherits(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addClass(identifier, parent, inherits, startLine, endLine);
        return identifier;
    }

    private void extractVariablesFromClass(CPP14Parser.ClassSpecifierContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.memberSpecification() == null) {
            return;
        }
        for (CPP14Parser.MemberdeclarationContext memberCtx : ctx.memberSpecification().memberdeclaration()) {
            if (memberCtx.functionDefinition() != null) {
                visitFunctionDefinition(memberCtx.functionDefinition(), parentIdentifier);
                continue;
            }

            if (memberCtx.declSpecifierSeq() != null) {
                for (CPP14Parser.DeclSpecifierContext declSpec : memberCtx.declSpecifierSeq().declSpecifier()) {
                    if (declSpec.typeSpecifier() != null && declSpec.typeSpecifier().classSpecifier() != null) {
                        // Recursive call to extract inner class members**
                        visitClassSpecifier(declSpec.typeSpecifier().classSpecifier(), parentIdentifier);
                    }
                }
            }

            if (memberCtx.memberDeclaratorList() != null && memberCtx.declSpecifierSeq() != null) {
                extractVariableElement(memberCtx, parentIdentifier);
            }
        }
    }

    private void extractVariableElement(CPP14Parser.MemberdeclarationContext ctx, ElementIdentifier parentIdentifier) {
        String variableType = ctx.declSpecifierSeq().getText();
        ElementIdentifier parent = parentIdentifier;
        List<String> varNames = extractVariableNames(ctx.memberDeclaratorList());
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addVariables(varNames, path, variableType, parent, startLine, endLine);
    }

    private void extractVariableElement(CPP14Parser.SimpleDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        String variableType = ctx.declSpecifierSeq().getText();
        ElementIdentifier parent = parentIdentifier;
        List<String> varNames = extractVariableNames(ctx.initDeclaratorList());
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addVariables(varNames, path, variableType, parent, startLine, endLine);
    }

    private List<String> extractVariableNames(CPP14Parser.MemberDeclaratorListContext ctx) {
        List<String> varNames = new ArrayList<>();
        for (CPP14Parser.MemberDeclaratorContext memberDec : ctx.memberDeclarator()) {
            // Skip if it is a function or Constructor
            if (memberDec.declarator().pointerDeclarator().noPointerDeclarator() != null && memberDec.declarator()
                    .pointerDeclarator().noPointerDeclarator().parametersAndQualifiers() != null) {
                continue;
            }
            varNames.add(memberDec.declarator().getText());
        }
        return varNames;
    }

    private List<String> extractVariableNames(CPP14Parser.InitDeclaratorListContext ctx) {
        List<String> varNames = new ArrayList<>();
        for (CPP14Parser.InitDeclaratorContext initDec : ctx.initDeclarator()) {
            // Skip if it is a function or Constructor
            if (initDec.declarator().pointerDeclarator().noPointerDeclarator() != null && initDec.declarator()
                    .pointerDeclarator().noPointerDeclarator().parametersAndQualifiers() != null) {
                continue;
            }
            varNames.add(initDec.declarator().getText());
        }
        return varNames;
    }

    private List<String> getInherits(CPP14Parser.ClassSpecifierContext ctx) {
        List<String> inherits = new ArrayList<>();
        if (ctx.classHead().baseClause() != null) {
            for (CPP14Parser.BaseSpecifierContext base : ctx.classHead().baseClause().baseSpecifierList()
                    .baseSpecifier()) {
                String baseClass = base.getText();

                if (base.accessSpecifier() != null) {
                    String accessSpecifier = base.accessSpecifier().getText();
                    baseClass = baseClass.replace(accessSpecifier, "").trim();
                }
                inherits.add(baseClass);
            }
        }
        return inherits;
    }

    private void addVariables(List<String> varNames, String path, String variableType, ElementIdentifier parent,
            int startLine, int endLine) {
        for (String varName : varNames) {
            addVariableElement(varName, path, variableType, parent, startLine, endLine);
        }
    }

    private void addVariableElement(String varName, String path, String variableType, ElementIdentifier parent,
            int startLine, int endLine) {
        VariableElement variable = new VariableElement(varName, path, variableType, parent, startLine, endLine);
        elementManager.addVariable(variable);
    }

    private void addFunction(ElementIdentifier identifier, ElementIdentifier parent, int startLine, int endLine) {
        Element function = new Element(identifier, parent, startLine, endLine);
        elementManager.addFunction(function);
    }

    private void addClass(ElementIdentifier identifier, ElementIdentifier parent, List<String> inherits, int startLine, int endLine) {
        ClassElement cppClassElement = new ClassElement(identifier, parent, startLine, endLine, inherits);
        elementManager.addClass(cppClassElement);
    }

    private void addNamespace(ElementIdentifier identifier, ElementIdentifier parent, int startLine, int endLine) {
        Element namespace = new Element(identifier, parent, startLine, endLine);
        elementManager.addNamespace(namespace);
    }

    private void addFile(TranslationUnitContext ctx) {
        String name = PathExtractor.extractNameFromPath(ctx);
        String path = PathExtractor.extractPath(ctx);
        Type type = Type.FILE;
        Element file = new Element(name, path, type);
        elementManager.addFile(file);
    }

    private ElementIdentifier getClassIdentifier(CPP14Parser.ClassSpecifierContext ctx) {
        String name = "anonymous";
        if (ctx.classHead().classHeadName() != null) {
            name = ctx.classHead().classHeadName().getText();
        } else if (ctx.classHead().classVirtSpecifier() != null) {
            name = ctx.classHead().classVirtSpecifier().getText();
        }
        String path = PathExtractor.extractPath(ctx);
        return new ElementIdentifier(name, path, Type.CLASS);
    }
}
