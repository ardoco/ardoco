/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.java.finder;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

@Deterministic
public class MethodDeclarationFinder extends ASTVisitor {

    private Set<MethodDeclaration> methodDeclarations = new LinkedHashSet<>();

    public static Set<MethodDeclaration> find(ASTNode node) {
        MethodDeclarationFinder finder = new MethodDeclarationFinder();
        node.accept(finder);
        return finder.methodDeclarations;
    }

    @Override
    public boolean visit(MethodDeclaration methodDeclaration) {
        methodDeclarations.add(methodDeclaration);
        return super.visit(methodDeclaration);
    }
}
