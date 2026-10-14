package org.example.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import org.example.ExpressionLexer;
import org.example.ExpressionParser;
import org.example.graph.DependencyGraph;
import org.example.model.App;
import org.example.model.Field;
import org.example.model.Form;

import java.util.*;

public class DependencyAnalyzer {

    private static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e) {
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
        if (field == null || field.getFormula() == null
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

        Map<String, Form> forms = new LinkedHashMap<>();
        for (Form form : app.getForms()) {
            if (form != null && form.getName() != null) {
                forms.put(form.getName(), form);
            }
        }

        for (Form form : forms.values()) {
            if (form.getFields() == null) {
                continue;
            }

            for (Field field : form.getFields()) {
                if (field == null || field.getName() == null) {
                    continue;
                }

                String source = qualify(form.getName(), field.getName());

                // Formula dependencies, including lookup and subform paths.
                for (String reference : analyzeField(field)) {
                    for (String target : resolveReference(
                            form, reference, forms)) {
                        graph.addDependency(source, target);
                    }
                }

                // A subform field depends on the fields in its child form.
                if (field.getSubform() != null
                        && forms.containsKey(field.getSubform())) {
                    Form child = forms.get(field.getSubform());
                    if (child.getFields() != null) {
                        for (Field childField : child.getFields()) {
                            if (childField != null
                                    && childField.getName() != null) {
                                graph.addDependency(
                                        source,
                                        qualify(child.getName(), childField.getName()));
                            }
                        }
                    }
                }
            }
        }

        return graph;
    }

    private List<String> resolveReference(
            Form currentForm,
            String reference,
            Map<String, Form> forms) {

        if (reference == null || reference.isBlank()) {
            return List.of();
        }

        String[] parts = reference.split("\\.");
        List<String> result = new ArrayList<>();

        // A simple reference belongs to the current form.
        if (parts.length == 1) {
            result.add(qualify(currentForm.getName(), parts[0]));
            return result;
        }

        // Explicit Form.Field reference.
        if (forms.containsKey(parts[0])) {
            result.add(qualify(parts[0], parts[1]));
            return result;
        }

        // Resolve a lookup or subform field path, e.g. Customer.Name
        // or Items.LineTotal.
        Field relationship = findField(currentForm, parts[0]);

        if (relationship != null && relationship.getLookup() != null) {
            result.add(qualify(relationship.getLookup(), parts[1]));
        } else if (relationship != null
                && relationship.getSubform() != null) {
            result.add(qualify(relationship.getSubform(), parts[1]));
        } else {
            // Preserve unresolved paths for later validation.
            result.add(reference);
        }

        return result;
    }

    private Field findField(Form form, String fieldName) {
        if (form == null || form.getFields() == null) {
            return null;
        }

        for (Field field : form.getFields()) {
            if (field != null && fieldName.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    private String qualify(String formName, String fieldName) {
        if (formName == null || formName.isBlank()) {
            return fieldName;
        }
        return formName + "." + fieldName;
    }
}