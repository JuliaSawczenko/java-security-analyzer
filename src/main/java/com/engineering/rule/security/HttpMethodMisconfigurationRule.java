package com.engineering.rule.security;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class HttpMethodMisconfigurationRule implements SecurityRule {

    private static final Set<String> SAFE  = Set.of("GET", "HEAD", "OPTIONS");
    private static final Set<String> UNSAFE = Set.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    public String getId() {
        return "SEC_HTTP_METHOD_CONFIG";
    }

    @Override
    public void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector) {
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            for (AnnotationExpr ann : method.getAnnotations()) {
                if (!"RequestMapping".equals(ann.getNameAsString())) {
                    continue;
                }

                if (ann.isNormalAnnotationExpr()) {
                    NormalAnnotationExpr nae = ann.asNormalAnnotationExpr();
                    List<MemberValuePair> pairs = nae.getPairs();
                    MemberValuePair methodPair = pairs.stream()
                            .filter(p -> "method".equals(p.getNameAsString()))
                            .findFirst()
                            .orElse(null);

                    if (methodPair == null) {
                        collector.report(this, method,
                                "Brak określonych metod HTTP w @RequestMapping; domyślnie wszystkie (bezpieczne i niebezpieczne) są dozwolone");
                    } else {
                        Set<String> found = extractMethods(methodPair.getValue());
                        boolean hasSafe  = !intersection(found, SAFE).isEmpty();
                        boolean hasUnsafe = !intersection(found, UNSAFE).isEmpty();

                        if (hasSafe && hasUnsafe) {
                            collector.report(this, method,
                                    "Mieszanie bezpiecznych i niebezpiecznych metod HTTP w @RequestMapping: "
                                            + found + "; rozważ ograniczenie metod niebezpiecznych do operacji zmieniających stan");
                        }
                    }

                } else {
                    collector.report(this, method,
                            "Brak określonych metod HTTP w @RequestMapping; domyślnie wszystkie (bezpieczne i niebezpieczne) są dozwolone");
                }
            }
        });
    }

    private Set<String> extractMethods(Expression expr) {
        Set<String> result = new HashSet<>();
        if (expr.isArrayInitializerExpr()) {
            expr.asArrayInitializerExpr().getValues()
                    .forEach(e -> collectFrom(e, result));
        } else {
            collectFrom(expr, result);
        }
        return result;
    }

    private void collectFrom(Expression expr, Set<String> into) {
        if (expr.isFieldAccessExpr()) {
            into.add(expr.asFieldAccessExpr().getNameAsString());
        } else if (expr.isNameExpr()) {
            into.add(expr.asNameExpr().getNameAsString());
        }
    }

    private static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> r = new HashSet<>(a);
        r.retainAll(b);
        return r;
    }
}