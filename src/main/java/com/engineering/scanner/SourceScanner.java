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
 * Klasa odpowiedzialna za przeszukiwanie katalogu źródeł Java,
 * parsowanie plików do AST oraz stosowanie reguł bezpieczeństwa.
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
     * Przechodzi przez katalog źródłowy, znajduje wszystkie pliki .java,
     * parsuje je do AST i stosuje podane reguły.
     *
     * @param sourceDir katalog główny z kodem źródłowym Java
     * @param rules     lista reguł do zastosowania
     * @param collector kolektor, do którego trafiają wszystkie problemy
     * @throws IOException w przypadku błędów odczytu plików
     */
    public void scan(Path sourceDir, List<SecurityRule> rules, FindingCollector collector) throws IOException {
        Files.walk(sourceDir)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        parseAndApply(path, rules, collector);
                    } catch (IOException e) {
                        System.err.println("Błąd odczytu pliku: " + path);
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Parsuje pojedynczy plik i stosuje do niego wszystkie reguły.
     *
     * @param file      ścieżka do pliku .java
     * @param rules     lista reguł do wykonania
     * @param collector kolektor na zgłoszone problemy
     * @throws IOException w przypadku błędu odczytu
     */
    private void parseAndApply(Path file, List<SecurityRule> rules, FindingCollector collector) throws IOException {
        ParseResult<CompilationUnit> result = javaParser.parse(file);
        if (result.isSuccessful() && result.getResult().isPresent()) {
            CompilationUnit cu = result.getResult().get();
            JavaParserFacade facade = JavaParserFacade.get(typeSolver);
            // Dla każdej reguły wywołujemy metodę apply
            for (SecurityRule rule : rules) {
                rule.apply(cu, facade, collector);
            }
        } else {
            System.err.println("Nie udało się sparsować pliku " + file + ":");
            result.getProblems().forEach(p -> System.err.println("  - " + p));
        }
    }
}