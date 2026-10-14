
package org.example;

import org.example.generator.SyntheticAppGenerator;
import org.example.loader.AppLoader;
import org.example.model.App;
import org.example.model.Form;
import org.example.model.Field;
import org.example.model.Validation;
import org.example.model.Report;
import org.example.model.Workflow;

import org.example.analysis.AppDependencyAnalyzer;
import org.example.analysis.UnknownFieldReference;
import org.example.analysis.ImpactAnalyzer;
import org.example.analysis.ImpactAnalysisResult;
import org.example.parser.DependencyAnalyzer;
import org.example.graph.DependencyGraph;

public class Main {

    public static void main(String[] args) throws Exception {

        // 1. Generate synthetic application data
        SyntheticAppGenerator generator = new SyntheticAppGenerator();

        App generatedApp = generator.generate(10, 100);
        generator.saveAsJson(generatedApp, "generated-app.json");

        System.out.println("Generated synthetic app successfully!");

        // 2. Load the generated application
        AppLoader loader = new AppLoader();
        App app = loader.load("generated-app.json");

        System.out.println("\nApp name: " + app.getName());

        int totalFields = 0;

        for (Form form : app.getForms()) {
            totalFields += form.getFields().size();
        }

        System.out.println("Number of forms: " + app.getForms().size());
        System.out.println("Total fields: " + totalFields);

        // 3. Print forms, fields, and validations
        System.out.println("\n========== APPLICATION DETAILS ==========");

        for (Form form : app.getForms()) {

            System.out.println("\nForm: " + form.getName());

            for (Field field : form.getFields()) {
                System.out.println(
                        "Field: " + field.getName()
                                + "\nType: " + field.getType()
                );
            }

            if (form.getValidations() != null) {
                for (Validation validation : form.getValidations()) {
                    System.out.println(
                            "Validation: " + validation.getName()
                                    + "\nCondition: " + validation.getCondition()
                    );
                }
            }
        }

        // 4. Print reports
        System.out.println("\n========== REPORTS ==========");

        if (app.getReports() != null) {
            for (Report report : app.getReports()) {
                System.out.println(
                        "Report: " + report.getName()
                                + " | Filter: " + report.getFilter()
                                + " | Sort: " + report.getSort()
                );

                System.out.println("Columns: " + report.getColumns());
            }
        }

        // 5. Print workflows
        System.out.println("\n========== WORKFLOWS ==========");

        if (app.getWorkflows() != null) {
            for (Workflow workflow : app.getWorkflows()) {
                System.out.println(
                        "Workflow: " + workflow.getName()
                                + " | Condition: " + workflow.getCondition()
                                + " | Action: " + workflow.getAction()
                );
            }
        }

        // 6. Validate field references
        System.out.println("\n========== FIELD REFERENCE VALIDATION ==========");

        AppDependencyAnalyzer appAnalyzer = new AppDependencyAnalyzer();

        var unknownReferences = appAnalyzer.findUnknownReferences(app);

        if (unknownReferences.isEmpty()) {
            System.out.println(
                    "Validation passed: no unknown field references."
            );
        } else {
            System.out.println("Unknown field references found:");

            for (UnknownFieldReference reference : unknownReferences) {
                System.out.println(reference);
            }
        }

        // 7. Parse formulas and build the dependency graph
        System.out.println("\n========== DEPENDENCY GRAPH ==========");

        DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer();

        DependencyGraph graph = dependencyAnalyzer.buildGraph(app);

        graph.printGraph();

        // 8. Calculate evaluation order
        System.out.println("\n========== EVALUATION ORDER ==========");

        try {
            System.out.println(graph.getEvaluationOrder());
        } catch (IllegalStateException e) {
            System.out.println(
                    "Cannot determine evaluation order: " + e.getMessage()
            );
        }

        // 9. Perform impact analysis using qualified field names
        System.out.println("\n========== IMPACT ANALYSIS ==========");

        String changedField = null;

        for (Form form : app.getForms()) {
            for (Field field : form.getFields()) {

                String qualifiedFieldName =
                        form.getName() + "." + field.getName();

                if (!graph.getDependents(qualifiedFieldName).isEmpty()) {
                    changedField = qualifiedFieldName;
                    break;
                }
            }

            if (changedField != null) {
                break;
            }
        }

        if (changedField == null) {
            System.out.println(
                    "No field with dependents was found. "
                            + "Check the generated formulas and dependency graph."
            );
        } else {
            System.out.println("Changed field: " + changedField);

            try {
                ImpactAnalyzer impactAnalyzer = new ImpactAnalyzer(graph);

                ImpactAnalysisResult result =
                        impactAnalyzer.analyze(changedField);

                System.out.println(
                        "Direct impact: " + result.getDirectImpact()
                );

                System.out.println(
                        "Indirect impact: " + result.getIndirectImpact()
                );

                System.out.println(
                        "Impact paths: " + result.getImpactPaths()
                );

            } catch (IllegalStateException e) {
                System.out.println(
                        "Impact analysis could not complete: " + e.getMessage()
                );
            }
        }

        System.out.println("\n========== ANALYSIS COMPLETED ==========");
    }
}
