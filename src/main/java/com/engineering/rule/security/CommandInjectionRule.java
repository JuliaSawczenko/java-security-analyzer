package com.engineering.rule.security;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.springframework.stereotype.Component;

@Component
public class CommandInjectionRule implements SecurityRule {

    @Override
    public String getId() {
        return "SEC_COMMAND_INJECTION";
    }

    @Override
    public void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector) {
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            if (!"exec".equals(call.getNameAsString())) {
                return;
            }
            call.getScope().ifPresent(scope -> {
                String text = scope.toString();
                if (!text.contains("Runtime.getRuntime")) {
                    return;
                }
                for (Expression arg : call.getArguments()) {
                    if (arg instanceof BinaryExpr bin &&
                            bin.getOperator() == BinaryExpr.Operator.PLUS) {
                        collector.report(
                                this,
                                call,
                                "Wykryto możliwe wstrzyknięcie polecenia: do Runtime.exec() przekazano sklejany ciąg znaków"
                        );
                        break;
                    }
                }
            });
        });
    }
}