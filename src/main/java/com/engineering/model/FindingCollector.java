package com.engineering.model;

import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import java.util.ArrayList;
import java.util.List;
import com.github.javaparser.ast.Node;
import lombok.Getter;
import org.springframework.stereotype.Component;


/**
 * Zbiera wszystkie wykryte problemy zgłaszane przez reguły bezpieczeństwa.
 */
@Getter
@Component
public class FindingCollector {

    /**
     * Lista zgłoszonych problemów przez reguły.
     */
    private final List<Finding> findings = new ArrayList<>();

    /**
     * Zgłasza nowe problemy dla podanej reguły w miejscu wskazanego węzła AST.
     *
     * @param rule    reguła, która wywołała zgłoszenie
     * @param node    węzeł AST, w którym wykryto problem
     * @param message opis problemu lub rekomendacja naprawy
     */
    public void report(SecurityRule rule, Node node, String message) {
        int line = node.getBegin().map(p -> p.line).orElse(-1);
        String file = node.findCompilationUnit()
                .flatMap(CompilationUnit::getStorage)
                .map(s -> s.getPath().toString())
                .orElse("Unknown");
        findings.add(new Finding(rule.getId(), file, line, message));
    }
}
