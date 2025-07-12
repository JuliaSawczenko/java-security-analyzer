package com.engineering.rule.security;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.springframework.stereotype.Component;

@Component
public class InsecureRandomRule implements SecurityRule {

    @Override
    public String getId() {
        return "SEC_INSECURE_RANDOM";
    }

    @Override
    public void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector) {
        cu.findAll(ObjectCreationExpr.class).forEach(oce -> {
            try {
                String type = facade.getType(oce).describe();
                if ("java.util.Random".equals(type)) {
                    collector.report(
                            this,
                            oce,
                            "Wykryto użycie niezabezpieczonego java.util.Random; zaleca się użycie java.security.SecureRandom"
                    );
                }
            } catch (Exception ignored) {
                // jeśli nie uda się rozwiązać typu, pomijamy
            }
        });
    }
}