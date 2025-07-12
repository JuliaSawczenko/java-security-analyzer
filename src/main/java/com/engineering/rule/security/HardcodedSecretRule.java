package com.engineering.rule.security;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class HardcodedSecretRule implements SecurityRule {

    private static final Set<String> KEYWORDS = Set.of(
            "password", "passwd", "pwd", "secret", "hasło", "klucz",
            "apikey", "api_key", "token", "key",
            "accesskey", "secretkey"
    );

    @Override
    public String getId() {
        return "SEC_HARDCODED_SECRET";
    }

    @Override
    public void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector) {
        cu.findAll(VariableDeclarator.class).forEach(vd -> {
            if (!vd.getType().isClassOrInterfaceType()) return;
            if (!vd.getType().asString().equals("String")) return;

            vd.getInitializer().ifPresent(init -> {
                if (init.isStringLiteralExpr()) {
                    String varName = vd.getNameAsString().toLowerCase();
                    if (KEYWORDS.stream().anyMatch(varName::contains)) {
                        collector.report(
                                this,
                                vd,
                                String.format(
                                        "W zmiennej '%s' wykryto hardcoded sekret; rozważ przeniesienie go do 'secure vault'",
                                        vd.getNameAsString()
                                )
                        );
                    }
                }
            });
        });
    }
}