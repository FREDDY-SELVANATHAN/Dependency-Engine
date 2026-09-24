package org.example;

import java.util.ArrayList;
import java.util.List;

public class AppDependencyAnalyzer {

    private final DependencyAnalyzer dependencyAnalyzer;

    public AppDependencyAnalyzer() {
        this.dependencyAnalyzer = new DependencyAnalyzer();
    }

    public List<FieldDependency> analyze(App app) {

        List<FieldDependency> results = new ArrayList<>();

        for (Form form : app.getForms()) {

            for (Field field : form.getFields()) {

                if (field.getFormula() == null || field.getFormula().isBlank()) {
                    continue;
                }

                List<String> dependencies =
                        dependencyAnalyzer.analyzeField(field);

                FieldDependency dependency = new FieldDependency(
                        form.getName() + "." + field.getName(),
                        field.getFormula(),
                        dependencies
                );

                results.add(dependency);
            }
        }

        return results;
    }
}