package com.engineering.model;

import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import java.util.ArrayList;
import java.util.List;
import com.github.javaparser.ast.Node;
import lombok.Getter;
import org.springframework.stereotype.Component;


@Getter
@Component
public class FindingCollector {

    /**
     * A collection of findings reported by the security rules.
     */
    private final List<Finding> findings = new ArrayList<>();

    /**
     * Reports a new finding for the given rule at the location of the AST node.
     *
     * @param rule    the rule that triggered the finding
     * @param node    the AST node where the issue was detected
     * @param message a human-readable description or recommendation
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