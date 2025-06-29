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
                var type = facade.getType(oce).describe();
                if ("java.util.Random".equals(type)) {
                    collector.report(this, oce,
                        "Use of insecure Random; prefer java.security.SecureRandom");
                }
            } catch (Exception ignored) { }
        });
    }
}