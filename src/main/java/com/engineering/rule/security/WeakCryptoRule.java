package com.engineering.rule.security;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class WeakCryptoRule implements SecurityRule {

    private static final Set<String> WEAK = Set.of(
            "MD5", "SHA-1", "DES", "DES/ECB", "DESede", "RC4", "AES/ECB"
    );

    @Override
    public String getId() {
        return "SEC_WEAK_CRYPTO";
    }

    @Override
    public void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector) {
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            if (!"getInstance".equals(call.getNameAsString())) {
                return;
            }
            ResolvedMethodDeclaration decl;
            try {
                decl = call.resolve();
            } catch (Exception e) {
                return;
            }
            String cls = decl.declaringType().getQualifiedName();
            if (!cls.equals("java.security.MessageDigest") && !cls.equals("javax.crypto.Cipher")) {
                return;
            }
            call.getArguments().stream()
                    .filter(StringLiteralExpr.class::isInstance)
                    .map(StringLiteralExpr.class::cast)
                    .map(StringLiteralExpr::getValue)
                    .map(String::toUpperCase)
                    .forEach(alg -> {
                        WEAK.stream()
                                .filter(alg::contains)
                                .findFirst()
                                .ifPresent(bad -> collector.report(
                                        this,
                                        call,
                                        String.format(
                                                "Wykryto użycie słabego algorytmu '%s' w %s; rozważ użycie SHA-256 lub AES/GCM",
                                                alg, cls
                                        )
                                ));
                    });
        });
    }
}
