package com.engineering.rule.maintability;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.springframework.stereotype.Component;

@Component
public class LongMethodRule implements SecurityRule {

    private static final int MAX_STATEMENTS = 50;

    @Override
    public String getId() {
        return "MNT_LONG_METHOD";
    }

    @Override
    public void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector) {
        cu.findAll(MethodDeclaration.class).forEach(m -> {
            int count = m.findAll(Statement.class).size();
            if (count > MAX_STATEMENTS) {
                collector.report(
                    this,
                    m,
                    String.format("Method '%s' has %d statements (max=%d); consider breaking it up",
                        m.getName(), count, MAX_STATEMENTS));
            }
        });
    }
}