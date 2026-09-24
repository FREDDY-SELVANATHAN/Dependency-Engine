package org.example;

import java.util.ArrayList;
import java.util.List;

public class DependencyListener extends ExpressionBaseListener {

    private final List<String> dependencies = new ArrayList<>();
    private final List<Integer> dependencyPositions = new ArrayList<>();

    @Override
    public void enterFieldReference(ExpressionParser.FieldReferenceContext ctx) {

        String field = ctx.getText();

        dependencies.add(field);

        dependencyPositions.add(
                ctx.getStart().getStartIndex()
        );
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public List<Integer> getDependencyPositions() {
        return dependencyPositions;
    }
}