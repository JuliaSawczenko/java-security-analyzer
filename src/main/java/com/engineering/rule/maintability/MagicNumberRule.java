package com.engineering.rule.maintability;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.springframework.stereotype.Component;

@Component
public class MagicNumberRule implements SecurityRule {

    @Override
    public String getId() {
        return "MNT_MAGIC_NUMBER";
    }

    @Override
    public void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector) {
        cu.findAll(IntegerLiteralExpr.class).forEach(lit -> {
            String val = lit.getValue();
            // pomijamy uniwersalne literały
            if (!val.equals("0") && !val.equals("1") && !val.equals("-1")) {
                collector.report(
                        this,
                        lit,
                        String.format(
                                "Wykryto magiczną liczbę '%s'; rozważ zastąpienie stałą o opisowej nazwie",
                                val
                        )
                );
            }
        });
    }
}