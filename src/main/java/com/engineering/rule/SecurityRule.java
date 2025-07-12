package com.engineering.rule;

import com.engineering.model.FindingCollector;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

/**
 * Reprezentuje pojedynczą regułę analizy AST.
 */
public interface SecurityRule {

    /**
     * Zwraca unikalny identyfikator reguły, np. "SEC_SQL_INJECTION".
     *
     * @return identyfikator reguły
     */
    String getId();

    /**
     * Stosuje tę regułę do przekazanego drzewa składniowego (AST),
     * zgłaszając wykryte problemy do kolektora.
     *
     * @param cu        przeparsowany CompilationUnit (korzeń AST)
     * @param facade    JavaParserFacade służący do rozwiązywania typów
     * @param collector kolektor, do którego trafiają zgłoszone problemy
     */
    void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector);
}