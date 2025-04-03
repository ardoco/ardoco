/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.java.finder;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumDeclaration;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

@Deterministic
public class EnumDeclarationFinder extends ASTVisitor {

    private Set<EnumDeclaration> enumDeclarations = new LinkedHashSet<>();

    public static Set<EnumDeclaration> find(ASTNode node) {
        EnumDeclarationFinder finder = new EnumDeclarationFinder();
        node.accept(finder);
        return finder.enumDeclarations;
    }

    @Override
    public boolean visit(EnumDeclaration enumDeclaration) {
        enumDeclarations.add(enumDeclaration);
        return super.visit(enumDeclaration);
    }
}
