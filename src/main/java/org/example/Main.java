
package org.example;

import org.example.loader.AppLoader;
import org.example.model.App;
import org.example.model.Form;
import org.example.model.Field;

import org.example.analysis.AppDependencyAnalyzer;
import org.example.analysis.UnknownFieldReference;
import org.example.analysis.FormulaValidationError;
import org.example.analysis.ImpactAnalyzer;
import org.example.analysis.ImpactAnalysisResult;
import org.example.analysis.ComponentDependency;

import org.example.parser.DependencyAnalyzer;
import org.example.graph.DependencyGraph;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length < 1 || args.length > 2) {
            System.out.println("Usage:");
            System.out.println("Main <app.json> [Form.Field]");
            return;
        }

        String appPath = args[0];
        String changedField = args.length == 2 ? args[1] : null;

        // 1. Load application
        AppLoader loader = new AppLoader();
        App app = loader.load(appPath);

        System.out.println("App name: " + app.getName());

        int totalFields = 0;

        if (app.getForms() != null) {
            for (Form form : app.getForms()) {
                if (form != null && form.getFields() != null) {
                    totalFields += form.getFields().size();
                }
            }
        }

        System.out.println("Number of forms: "
                + (app.getForms() == null ? 0 : app.getForms().size()));
        System.out.println("Total fields: " + totalFields);

        AppDependencyAnalyzer appAnalyzer =
                new AppDependencyAnalyzer();

        // 2. Validate field references
        System.out.println(
                "\n========== FIELD REFERENCE VALIDATION ==========");

        List<UnknownFieldReference> unknownReferences =
                appAnalyzer.findUnknownReferences(app);

        if (unknownReferences.isEmpty()) {
            System.out.println("No unknown formula field references.");
        } else {
            unknownReferences.forEach(System.out::println);
        }

        // 3. Validate formula syntax
        System.out.println("\n========== FORMULA VALIDATION ==========");

        List<FormulaValidationError> formulaErrors =
                appAnalyzer.findFormulaErrors(app);

        if (formulaErrors.isEmpty()) {
            System.out.println("No malformed formulas found.");
        } else {
            formulaErrors.forEach(System.out::println);
        }

        // 4. Analyze reports, validations, and workflows
        System.out.println("\n========== COMPONENT DEPENDENCIES ==========");

        List<ComponentDependency> componentDependencies =
                appAnalyzer.analyzeComponents(app);

        if (componentDependencies.isEmpty()) {
            System.out.println("No component dependencies found.");
        } else {
            for (ComponentDependency component : componentDependencies) {
                System.out.println(
                        component.getComponentType() + " | "
                                + component.getComponentName()
                                + " | Dependencies: "
                                + component.getDependencies()
                );
            }
        }

        // 5. Build dependency graph
        System.out.println("\n========== DEPENDENCY GRAPH ==========");

        DependencyAnalyzer dependencyAnalyzer =
                new DependencyAnalyzer();

        DependencyGraph graph =
                dependencyAnalyzer.buildGraph(app);

        graph.printGraph();

        // 6. Evaluation order
        System.out.println("\n========== EVALUATION ORDER ==========");

        try {
            System.out.println(graph.getEvaluationOrder());
        } catch (IllegalStateException e) {
            System.out.println(
                    "Cannot determine evaluation order: "
                            + e.getMessage()
            );
        }

        // 7. Impact analysis
        System.out.println("\n========== IMPACT ANALYSIS ==========");

        if (changedField == null) {
            System.out.println(
                    "No changed field provided. Pass Form.Field "
                            + "as the second argument."
            );

        } else if (!fieldExists(app, changedField)) {
            System.out.println("Unknown changed field: " + changedField);

        } else {
            System.out.println("Changed field: " + changedField);

            try {
                ImpactAnalyzer impactAnalyzer =
                        new ImpactAnalyzer(graph);

                ImpactAnalysisResult result =
                        impactAnalyzer.analyze(changedField);

                System.out.println(
                        "Direct formula impact: "
                                + result.getDirectImpact()
                );

                System.out.println(
                        "Indirect formula impact: "
                                + result.getIndirectImpact()
                );

                System.out.println(
                        "Impact paths: " + result.getImpactPaths()
                );

                String shortFieldName =
                        changedField.substring(
                                changedField.indexOf('.') + 1
                        );

                System.out.println("Affected components:");

                boolean found = false;

                for (ComponentDependency component : componentDependencies) {
                    boolean affected =
                            component.getDependencies().contains(changedField)
                                    || component.getDependencies()
                                    .contains(shortFieldName);

                    if (affected) {
                        System.out.println(
                                "  " + component.getComponentType()
                                        + " | " + component.getComponentName()
                        );
                        found = true;
                    }
                }

                if (!found) {
                    System.out.println(
                            "No directly affected components found."
                    );
                }

            } catch (IllegalStateException e) {
                System.out.println(
                        "Impact analysis failed: " + e.getMessage()
                );
            }
        }

        System.out.println("\n========== ANALYSIS COMPLETED ==========");
    }

    private static boolean fieldExists(App app, String qualifiedName) {

        if (app == null || app.getForms() == null
                || qualifiedName == null) {
            return false;
        }

        for (Form form : app.getForms()) {
            if (form == null || form.getFields() == null) {
                continue;
            }

            for (Field field : form.getFields()) {
                if (field != null
                        && qualifiedName.equals(
                        form.getName() + "." + field.getName())) {
                    return true;
                }
            }
        }

        return false;
    }
}
