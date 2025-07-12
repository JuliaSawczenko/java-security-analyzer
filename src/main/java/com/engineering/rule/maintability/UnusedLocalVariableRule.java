package com.engineering.rule.maintability;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.springframework.stereotype.Component;

@Component
public class UnusedLocalVariableRule implements SecurityRule {

    @Override
    public String getId() {
        return "MNT_UNUSED_LOCAL";
    }

    @Override
    public void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector) {
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            method.findAll(VariableDeclarator.class).forEach(varDecl -> {
                String name = varDecl.getNameAsString();
                long occurrences = method.findAll(NameExpr.class).stream()
                        .filter(ne -> ne.getNameAsString().equals(name))
                        .count();
                if (occurrences <= 1) {
                    collector.report(
                            this,
                            varDecl,
                            String.format(
                                    "Zmienna lokalna '%s' nie jest używana; rozważ jej usunięcie",
                                    name
                            )
                    );
                }
            });
        });
    }
}