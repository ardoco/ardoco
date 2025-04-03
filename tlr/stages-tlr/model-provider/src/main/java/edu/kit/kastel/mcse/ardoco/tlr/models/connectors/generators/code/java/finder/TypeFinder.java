/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.java.finder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

public class TypeFinder extends ASTVisitor {

    private List<Type> types = new ArrayList<>();

    public static List<Type> find(ASTNode node) {
        TypeFinder finder = new TypeFinder();
        node.accept(finder);
        return finder.types;
    }

    @Override
    public boolean visit(SimpleType type) {
        types.add(type);
        return super.visit(type);
    }

    @Override
    public boolean visit(QualifiedType type) {
        types.add(type);
        return super.visit(type);
    }

    @Override
    public boolean visit(NameQualifiedType type) {
        types.add(type);
        return super.visit(type);
    }
}
