package com.engineering.rule.security;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Component;

@Component
public class CommandInjectionRule implements SecurityRule {

    @Override
    public String getId() {
        return "SEC_COMMAND_INJECTION";
    }

    @Override
    public void apply(CompilationUnit cu,
        com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade facade,
        FindingCollector collector) {
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            // Look for Runtime.getRuntime().exec(...)
            if (!"exec".equals(call.getNameAsString())) return;
            // Ensure it's Runtime.exec
            call.getScope().ifPresent(scope -> {
                String scopeStr = scope.toString();
                if (!scopeStr.contains("Runtime.getRuntime")) return;
                for (Expression arg : call.getArguments()) {
                    if (arg instanceof BinaryExpr bin &&
                        bin.getOperator() == BinaryExpr.Operator.PLUS) {
                        collector.report(this, call,
                            "Potential command‐injection: concatenated string passed to Runtime.exec()");
                    }
                }
            });
        });
    }
}