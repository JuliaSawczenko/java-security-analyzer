package com.engineering.rule;

import com.engineering.model.FindingCollector;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

public interface SecurityRule {
    /**
     * Unique identifier for the rule, e.g. "SEC_SQL_INJECTION".
     */
    String getId();

    /**
     * Applies this rule to the given AST, reporting any issues to the collector.
     *
     * @param cu        the parsed CompilationUnit (AST root)
     * @param facade    the JavaParserFacade for type resolution
     * @param collector where to report findings
     */
    void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector);
}