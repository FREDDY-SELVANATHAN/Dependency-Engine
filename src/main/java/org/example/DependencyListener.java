package org.example;

import java.util.ArrayList;
import java.util.List;

public class DependencyListener extends ExpressionBaseListener {

    private final List<String> dependencies = new ArrayList<>();

    @Override
    public void enterFieldReference(ExpressionParser.FieldReferenceContext ctx) {
        String field = ctx.getText();
        dependencies.add(field);
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}