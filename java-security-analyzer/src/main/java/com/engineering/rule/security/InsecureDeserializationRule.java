package com.engineering.rule.security;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.springframework.stereotype.Component;

@Component
public class InsecureDeserializationRule implements SecurityRule {

    @Override
    public String getId() {
        return "SEC_INSECURE_DESERIALIZATION";
    }

    @Override
    public void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector) {
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            if (!"readObject".equals(call.getNameAsString())) return;
            ResolvedMethodDeclaration decl;
            try {
                decl = call.resolve();
            } catch (Exception e) {
                return;
            }
            if ("java.io.ObjectInputStream".equals(decl.declaringType().getQualifiedName())) {
                collector.report(this, call,
                    "Unvalidated deserialization via ObjectInputStream.readObject;");
            }
        });
    }
}