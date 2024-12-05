package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


import org.apache.commons.compress.utils.FileNameUtils;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.Datatype;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParser;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParserBaseVisitor;

public class JavaVisitorManager extends JavaParserBaseVisitor<Void> {
    private final CodeItemRepository codeItemRepository;
    private final Path dir;
    private final Path file;

    private final SortedSet<Datatype> types;
    private final SortedSet<CodeItem> codeItems;

    private final JavaCompilationUnitExtractor compilationUnitExtractor;
    private final JavaPackageExtractor packageExtractor;
    private final JavaFieldExtractor fieldExtractor;
    private final JavaMethodExtractor methodExtractor;
    private final JavaMemberExtractor memberExtractor;
    private final JavaTypeExtractor typeExtractor;
    private final JavaVariableExtractor variableExtractor;
    private final JavaModuleExtractor moduleExtractor;

    public JavaVisitorManager(CodeItemRepository codeItemRepository, Path dir, Path file) {
        this.codeItemRepository = codeItemRepository;
        this.dir = dir;
        this.file = file;
        this.codeItems = new TreeSet<>();
        this.types = new TreeSet<>();

        this.compilationUnitExtractor = new JavaCompilationUnitExtractor(codeItemRepository);
        this.packageExtractor = new JavaPackageExtractor(codeItemRepository);
        this.fieldExtractor = new JavaFieldExtractor(codeItemRepository);
        this.methodExtractor = new JavaMethodExtractor(codeItemRepository);
        this.memberExtractor = new JavaMemberExtractor(codeItemRepository);
        this.typeExtractor = new JavaTypeExtractor(codeItemRepository);
        this.variableExtractor = new JavaVariableExtractor(codeItemRepository);
        this.moduleExtractor = new JavaModuleExtractor(codeItemRepository);
    }

    public CodeItem getCodeItem() {
        String fileNameWithoutExtension = FileNameUtils.getBaseName(file);
        String extension = FileNameUtils.getExtension(file);

        List<String> pathElements = new ArrayList<>();
        for (int i = 0; i < dir.getNameCount(); i++) {
            pathElements.add(dir.getName(i).toString());
        }

        CodeCompilationUnit codeCompilationUnit = new CodeCompilationUnit(
            codeItemRepository,
            fileNameWithoutExtension,
            types, 
            pathElements,
            extension,
            ProgrammingLanguage.JAVA);

        return codeCompilationUnit;
    }

    @Override 
    public Void visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        CodeItem codeItem = compilationUnitExtractor.visitCompilationUnit(ctx);
        if (codeItem != null) {
            codeItems.add(codeItem);
        }
        return super.visitCompilationUnit(ctx);
    }

    @Override
    public Void visitPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        CodeItem codeItem = packageExtractor.visitPackageDeclaration(ctx);
        if (codeItem != null) {
            codeItems.add(codeItem);
        }
        return super.visitPackageDeclaration(ctx);
    }

    @Override
    public Void visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        CodeItem codeItem = fieldExtractor.visitFieldDeclaration(ctx);
        if (codeItem != null) {
            codeItems.add(codeItem);
        }
        return super.visitFieldDeclaration(ctx);
    }

    @Override
    public Void visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        CodeItem codeItem = methodExtractor.visitMethodDeclaration(ctx);
        if (codeItem != null) {
            codeItems.add(codeItem);
        }
        return super.visitMethodDeclaration(ctx);
    }

    @Override
    public Void visitMemberDeclaration(JavaParser.MemberDeclarationContext ctx) {
        CodeItem codeItem = memberExtractor.visitMemberDeclaration(ctx);
        if (codeItem != null) {
            codeItems.add(codeItem);
        }
        return super.visitMemberDeclaration(ctx);
    }

    @Override
    public Void visitTypeDeclaration(JavaParser.TypeDeclarationContext ctx) {
        Datatype type = typeExtractor.visitTypeDeclaration(ctx);
        if (type != null) {
            types.add(type);
        }
        return super.visitTypeDeclaration(ctx);
    }

    @Override
    public Void visitVariableDeclarator(JavaParser.VariableDeclaratorContext ctx) {
        CodeItem codeItem = variableExtractor.visitVariableDeclarator(ctx);
        if (codeItem != null) {
            codeItems.add(codeItem);
        }
        return super.visitVariableDeclarator(ctx);
    }

    @Override
    public Void visitModuleDeclaration(JavaParser.ModuleDeclarationContext ctx) {
        CodeItem codeItem = moduleExtractor.visitModuleDeclaration(ctx);
        if (codeItem != null) {
            codeItems.add(codeItem);
        }
        return super.visitModuleDeclaration(ctx);
    }

    @Override
    public Void visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
        CodeItem codeItem = memberExtractor.visitClassBodyDeclaration(ctx);
        if (codeItem != null) {
            codeItems.add(codeItem);
        }
        return super.visitClassBodyDeclaration(ctx);
    }
} 
