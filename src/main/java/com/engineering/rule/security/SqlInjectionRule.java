package com.engineering.rule.security;

import com.engineering.model.FindingCollector;
import com.engineering.rule.SecurityRule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import java.util.HashSet;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class SqlInjectionRule implements SecurityRule {

    private static final Set<String> SQL_METHODS;
    static {
        SQL_METHODS = new HashSet<>();
        SQL_METHODS.add("execute");
        SQL_METHODS.add("executeQuery");
        SQL_METHODS.add("executeUpdate");
        SQL_METHODS.add("prepareStatement");
        SQL_METHODS.add("addBatch");
    }

    private static final Set<String> SQL_TYPES;
    static {
        SQL_TYPES = new HashSet<>();
        SQL_TYPES.add("java.sql.Statement");
        SQL_TYPES.add("java.sql.PreparedStatement");
        SQL_TYPES.add("java.sql.CallableStatement");
    }

    @Override
    public String getId() {
        return "SEC_SQL_INJECTION";
    }

    @Override
    public void apply(CompilationUnit cu, JavaParserFacade facade, FindingCollector collector) {
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            String methodName = call.getNameAsString();
            if (!SQL_METHODS.contains(methodName)) {
                return;
            }

            ResolvedMethodDeclaration decl;
            try {
                decl = call.resolve();
            } catch (Exception e) {
                return;
            }
            String declaringType = decl.declaringType().getQualifiedName();
            if (!SQL_TYPES.contains(declaringType)) {
                return;
            }

            for (Expression arg : call.getArguments()) {
                if (isStringConcat(arg)) {
                    collector.report(
                            this,
                            call,
                            String.format(
                                    "Możliwe ryzyko SQL Injection: konkatenacja ciągu SQL przekazana bezpośrednio do %s()",
                                    methodName
                            )
                    );
                }
                else if (arg instanceof NameExpr nameExpr) {
                    String varName = nameExpr.getNameAsString();
                    findVariableInitializer(cu, varName)
                            .filter(this::isStringConcat)
                            .forEach(init -> collector.report(
                                    this,
                                    call,
                                    String.format(
                                            "Możliwe ryzyko SQL Injection: zmienna '%s' zbudowana przez konkatenację przed wywołaniem %s()",
                                            varName, methodName
                                    )
                            ));
                }
            }
        });
    }

    /** Sprawdza, czy wyrażenie to konkatenacja Stringów poprzez '+' */
    private boolean isStringConcat(Expression expr) {
        return expr instanceof BinaryExpr bin
                && bin.getOperator() == BinaryExpr.Operator.PLUS;
    }

    /** Znajduje inicjalizator zmiennej o podanej nazwie w całym CU */
    private Stream<Expression> findVariableInitializer(CompilationUnit cu, String varName) {
        return cu.findAll(VariableDeclarator.class).stream()
                .filter(vd -> vd.getNameAsString().equals(varName))
                .map(VariableDeclarator::getInitializer)
                .flatMap(Optional::stream);
    }
}


