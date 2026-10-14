
package org.example.analysis;

import org.example.model.App;
import org.example.model.Field;
import org.example.model.Form;
import org.example.parser.DependencyAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppDependencyAnalyzer {

    private final DependencyAnalyzer dependencyAnalyzer;

    public AppDependencyAnalyzer() {
        this.dependencyAnalyzer = new DependencyAnalyzer();
    }

    public List<FieldDependency> analyze(App app) {

        List<FieldDependency> results = new ArrayList<>();

        for (Form form : app.getForms()) {
            for (Field field : form.getFields()) {

                if (field.getFormula() == null
                        || field.getFormula().isBlank()) {
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

    public List<UnknownFieldReference> findUnknownReferences(App app) {

        List<UnknownFieldReference> issues = new ArrayList<>();

        Map<String, Set<String>> fieldsByForm = new HashMap<>();
        Set<String> allFieldNames = new HashSet<>();

        // Collect field names by form and across the application.
        for (Form form : app.getForms()) {
            Set<String> fieldNames = new HashSet<>();

            for (Field field : form.getFields()) {
                fieldNames.add(field.getName());
                allFieldNames.add(field.getName());
            }

            fieldsByForm.put(form.getName(), fieldNames);
        }

        // Validate references in each formula.
        for (Form form : app.getForms()) {
            for (Field field : form.getFields()) {

                if (field.getFormula() == null
                        || field.getFormula().isBlank()) {
                    continue;
                }

                List<String> references =
                        dependencyAnalyzer.analyzeField(field);

                for (String reference : references) {

                    boolean exists;

                    if (reference.contains(".")) {
                        String[] parts = reference.split("\\.", -1);

                        exists = parts.length == 2
                                && !parts[0].isBlank()
                                && !parts[1].isBlank()
                                && fieldsByForm.containsKey(parts[0])
                                && fieldsByForm.get(parts[0])
                                .contains(parts[1]);
                    } else {
                        exists = allFieldNames.contains(reference);
                    }

                    if (!exists) {
                        issues.add(new UnknownFieldReference(
                                form.getName() + "." + field.getName(),
                                reference,
                                field.getFormula()
                        ));
                    }
                }
            }
        }

        return issues;
    }
}
