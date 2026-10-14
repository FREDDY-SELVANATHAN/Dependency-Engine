
package org.example.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import org.example.ExpressionLexer;
import org.example.ExpressionParser;
import org.example.graph.DependencyGraph;
import org.example.model.App;
import org.example.model.Field;
import org.example.model.Form;

import java.util.List;

public class DependencyAnalyzer {

    private static class ThrowingErrorListener
            extends BaseErrorListener {

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e
        ) {
            throw new IllegalArgumentException(
                    "Invalid formula at line " + line
                            + ", column " + charPositionInLine
                            + ": " + msg
            );
        }
    }

    public List<String> analyze(String formula) {
        if (formula == null || formula.isBlank()) {
            return List.of();
        }

        CharStream input = CharStreams.fromString(formula);

        ExpressionLexer lexer = new ExpressionLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ThrowingErrorListener());

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ExpressionParser parser = new ExpressionParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ThrowingErrorListener());

        ParseTree tree = parser.expression();

        DependencyListener listener = new DependencyListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        return List.copyOf(listener.getDependencies());
    }

    public List<String> analyzeField(Field field) {
        if (field == null
                || field.getFormula() == null
                || field.getFormula().isBlank()) {
            return List.of();
        }

        return analyze(field.getFormula());
    }

    public DependencyGraph buildGraph(App app) {
        DependencyGraph graph = new DependencyGraph();

        if (app == null || app.getForms() == null) {
            return graph;
        }

        for (Form form : app.getForms()) {
            if (form == null || form.getFields() == null) {
                continue;
            }

            String formName = form.getName();

            for (Field field : form.getFields()) {
                if (field == null || field.getName() == null) {
                    continue;
                }

                String source = qualify(formName, field.getName());

                List<String> dependencies = analyzeField(field);

                for (String dependency : dependencies) {
                    String target = qualifyDependency(formName, dependency);
                    graph.addDependency(source, target);
                }
            }
        }

        return graph;
    }

    private String qualify(String formName, String fieldName) {
        if (formName == null || formName.isBlank()) {
            return fieldName;
        }

        return formName + "." + fieldName;
    }

    private String qualifyDependency(
            String currentFormName,
            String dependency
    ) {
        if (dependency == null || dependency.isBlank()) {
            return dependency;
        }

        // A dotted reference is already qualified.
        if (dependency.contains(".")) {
            return dependency;
        }

        // An unqualified reference belongs to the current form.
        return qualify(currentFormName, dependency);
    }
}
