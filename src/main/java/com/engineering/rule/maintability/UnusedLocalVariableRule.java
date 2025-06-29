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
        cu.findAll(MethodDeclaration.class).forEach(m -> {
            m.findAll(VariableDeclarator.class).forEach(vd -> {
                String varName = vd.getNameAsString();
                long refs = m.findAll(NameExpr.class).stream()
                    .filter(ne -> ne.getNameAsString().equals(varName))
                    .count();
                if (refs <= 1) {
                    collector.report(
                        this,
                        vd,
                        "Local variable '" + varName + "' is never used; consider removing it");
                }
            });
        });
    }
}