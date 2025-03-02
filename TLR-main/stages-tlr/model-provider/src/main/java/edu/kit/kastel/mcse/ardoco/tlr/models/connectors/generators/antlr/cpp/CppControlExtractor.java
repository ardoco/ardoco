package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.cpp;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14ParserBaseVisitor;

public class CppControlExtractor extends CPP14ParserBaseVisitor<List<ControlElement>> {
    List<ControlElement> controls = new ArrayList<>();
    
    @Override
    public List<ControlElement> visitTranslationUnit(CPP14Parser.TranslationUnitContext ctx) {
        super.visitTranslationUnit(ctx);
        return controls;
    }

    @Override
    public List<ControlElement> visitDeclaration(CPP14Parser.DeclarationContext ctx) {
        if (ctx.functionDefinition() != null) {
            visitFunctionDefinition(ctx.functionDefinition());
        } else if (ctx.blockDeclaration() != null) {
            visitBlockDeclaration(ctx.blockDeclaration());
        }
        return controls;
    }

    @Override
    public List<ControlElement> visitFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {
        String name = ctx.declarator().getText();
        Parent parent = CppParentExtractor.getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        ControlElement controlElement = new ControlElement(name, path, parent);
        controlElement.setStartLine(startLine);
        controlElement.setEndLine(endLine);
        controls.add(controlElement);
        return controls;
    }

    @Override
    public List<ControlElement> visitBlockDeclaration(CPP14Parser.BlockDeclarationContext ctx) {
        if (ctx.simpleDeclaration() != null) {
            visitSimpleDeclaration(ctx.simpleDeclaration());
        }
        return controls;
    }

    @Override
    public List<ControlElement> visitSimpleDeclaration(CPP14Parser.SimpleDeclarationContext ctx) {
        if (isAssignment(ctx)) {
            return controls;
        }

        for (CPP14Parser.DeclSpecifierContext declSeq : ctx.declSpecifierSeq().declSpecifier()) {
            if (declSeq.typeSpecifier().classSpecifier() != null) {
                return extractControlsFromClass(declSeq.typeSpecifier().classSpecifier());
            }
        }

        if (ctx.initDeclaratorList() != null) {
            return extractNormalControlElement(ctx);
        }
    }

    private List<ControlElement> extractControlsFromClass(CPP14Parser.ClassSpecifierContext ctx) {
        if (ctx.memberSpecification() == null) {
            return controls;
        }
        for (CPP14Parser.MemberdeclarationContext memberCtx : ctx.memberSpecification().memberdeclaration()) {
            if (memberCtx.functionDefinition() == null) {
                continue;
            }
            String name = memberCtx.functionDefinition().getText();
            Parent parent = CppParentExtractor.getParent(memberCtx);
            String path = PathExtractor.extractPath(ctx);
            int startLine = memberCtx.getStart().getLine();
            int endLine = memberCtx.getStop().getLine();
            ControlElement controlElement = new ControlElement(name, path, parent);
            controlElement.setStartLine(startLine);
            controlElement.setEndLine(endLine);
            controls.add(controlElement);
        }
        return controls;
    }

    private List<ControlElement> extractNormalControlElement(CPP14Parser.SimpleDeclarationContext ctx) {
        // TODO:Check if it is correct
        String name = ctx.declSpecifierSeq().getText();
        Parent parent = CppParentExtractor.getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        ControlElement controlElement = new ControlElement(name, path, parent);
        controlElement.setStartLine(startLine);
        controlElement.setEndLine(endLine);
        controls.add(controlElement);
        return controls;
    }


    private boolean isAssignment(CPP14Parser.SimpleDeclarationContext ctx) {
        return ctx.declSpecifierSeq() == null;
    }
}
