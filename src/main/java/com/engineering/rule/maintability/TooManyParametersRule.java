package com.engineering.rule.maintability;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.springframework.stereotype.Component;

@Component
public class TooManyParametersRule implements SecurityRule {

    private static final int MAX_PARAMS = 5;

    @Override
    public String getId() {
        return "MNT_TOO_MANY_PARAMS";
    }

    @Override
    public void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector) {
        cu.findAll(MethodDeclaration.class).stream()
                .filter(m -> m.getParameters().size() > MAX_PARAMS)
                .forEach(m -> collector.report(
                        this,
                        m,
                        String.format(
                                "Metoda '%s' ma %d parametrów (maksymalnie %d) – rozważ podział na mniejsze metody",
                                m.getName(),
                                m.getParameters().size(),
                                MAX_PARAMS
                        )
                ));
    }
}