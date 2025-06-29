package com.engineering.scanner;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Scans Java source files in a directory, parses them into ASTs, and applies security rules.
 */
@Component
public class SourceScanner {

    private final JavaParser javaParser;
    private final CombinedTypeSolver typeSolver;

    public SourceScanner() {
        this.typeSolver = new CombinedTypeSolver();
        this.typeSolver.add(new ReflectionTypeSolver());

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration config = new ParserConfiguration()
            .setSymbolResolver(symbolSolver);
        this.javaParser = new JavaParser(config);
    }

    /**
     * Walks the source directory, parses each .java file, and applies all provided rules.
     *
     * @param sourceDir the root directory of Java sources
     * @param rules     the list of security rules to apply
     * @param collector the collector to gather findings
     * @throws IOException if file I/O errors occur
     */
    public void scan(Path sourceDir, List<SecurityRule> rules, FindingCollector collector) throws IOException {
        Files.walk(sourceDir)
            .filter(path -> path.toString().endsWith(".java"))
            .forEach(path -> {
                try {
                    parseAndApply(path, rules, collector);
                } catch (IOException e) {
                    System.err.println("Error reading file: " + path);
                    e.printStackTrace();
                }
            });
    }

    private void parseAndApply(Path file, List<SecurityRule> rules, FindingCollector collector) throws IOException {
        ParseResult<CompilationUnit> result = javaParser.parse(file);
        if (result.isSuccessful() && result.getResult().isPresent()) {
            CompilationUnit cu = result.getResult().get();

            JavaParserFacade facade = JavaParserFacade.get(typeSolver);
            for (SecurityRule rule : rules) {
                rule.apply(cu, facade, collector);
            }
        } else {
            System.err.println("Failed to parse " + file + ":");
            result.getProblems().forEach(p -> System.err.println("  - " + p));
        }
    }
}

