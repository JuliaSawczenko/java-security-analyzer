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
        // Look at every controller method
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            // Find @RequestMapping annotations
            for (AnnotationExpr ann : method.getAnnotations()) {
                if (!"RequestMapping".equals(ann.getNameAsString())) {
                    continue;
                }

                if (ann.isNormalAnnotationExpr()) {
                    NormalAnnotationExpr nae = ann.asNormalAnnotationExpr();
                    // Find the 'method' attribute
                    List<MemberValuePair> pairs = nae.getPairs();
                    MemberValuePair methodPair = pairs.stream()
                        .filter(p -> "method".equals(p.getNameAsString()))
                        .findFirst()
                        .orElse(null);

                    if (methodPair == null) {
                        // No 'method' → all HTTP methods allowed
                        collector.report(this, method,
                            "No HTTP methods specified on @RequestMapping; by default all methods (safe + unsafe) are allowed.");
                    } else {
                        // Extract the list of methods from the annotation value
                        Set<String> found = extractMethods(methodPair.getValue());
                        boolean hasSafe  = !intersection(found, SAFE).isEmpty();
                        boolean hasUnsafe = !intersection(found, UNSAFE).isEmpty();

                        if (hasSafe && hasUnsafe) {
                            collector.report(this, method,
                                "Mixed safe and unsafe HTTP methods on @RequestMapping: "
                                    + found + "; consider restricting unsafe methods to state-changing endpoints.");
                        }
                    }

                } else {
                    // Marker or single-member annotation without params → all methods
                    collector.report(this, method,
                        "No HTTP methods specified on @RequestMapping; by default all methods (safe + unsafe) are allowed.");
                }
            }
        });
    }

    /** Pulls out RequestMethod names from an annotation value. */
    private Set<String> extractMethods(Expression expr) {
        Set<String> result = new HashSet<>();
        if (expr.isArrayInitializerExpr()) {
            for (Expression e : expr.asArrayInitializerExpr().getValues()) {
                collectFrom(e, result);
            }
        } else {
            collectFrom(expr, result);
        }
        return result;
    }

    /** Handles a single RequestMethod enum value expression. */
    private void collectFrom(Expression expr, Set<String> into) {
        // Expect something like RequestMethod.POST
        if (expr.isFieldAccessExpr()) {
            FieldAccessExpr fa = expr.asFieldAccessExpr();
            into.add(fa.getNameAsString());
        } else if (expr.isNameExpr()) {
            // e.g. imported static
            into.add(expr.asNameExpr().getNameAsString());
        }
    }

    /** Helper: intersection of two sets */
    private static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> r = new HashSet<>(a);
        r.retainAll(b);
        return r;
    }
}