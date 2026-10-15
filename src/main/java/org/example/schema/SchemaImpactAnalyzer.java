package org.example.schema;

import org.example.analysis.AppDependencyAnalyzer;
import org.example.analysis.ComponentDependency;
import org.example.analysis.ImpactAnalysisResult;
import org.example.analysis.ImpactAnalyzer;
import org.example.graph.DependencyGraph;
import org.example.model.App;
import org.example.parser.DependencyAnalyzer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SchemaImpactAnalyzer {

    public List<SchemaImpactResult> analyze(
            App oldApp,
            App newApp) {

        SchemaChangeAnalyzer schemaAnalyzer =
                new SchemaChangeAnalyzer();

        DependencyAnalyzer dependencyAnalyzer =
                new DependencyAnalyzer();

        List<SchemaChange> changes =
                schemaAnalyzer.compare(oldApp, newApp);

        DependencyGraph oldGraph =
                dependencyAnalyzer.buildGraph(oldApp);

        DependencyGraph newGraph =
                dependencyAnalyzer.buildGraph(newApp);

        List<ComponentDependency> oldComponents =
                new AppDependencyAnalyzer().analyzeComponents(oldApp);

        List<ComponentDependency> newComponents =
                new AppDependencyAnalyzer().analyzeComponents(newApp);

        List<SchemaImpactResult> results = new ArrayList<>();

        for (SchemaChange change : changes) {
            String field = change.getField();

            boolean removed = change.getType().equals("REMOVED");

            DependencyGraph graph = removed ? oldGraph : newGraph;

            ImpactAnalysisResult impact =
                    new ImpactAnalyzer(graph).analyze(field);

            Set<String> affectedComponents = new LinkedHashSet<>();

            collectAffectedComponents(
                    affectedComponents, oldComponents, field);

            collectAffectedComponents(
                    affectedComponents, newComponents, field);

            results.add(new SchemaImpactResult(
                    change, impact, affectedComponents));        }

        return results;
    }

    private void collectAffectedComponents(
            Set<String> affected,
            List<ComponentDependency> components,
            String changedField) {

        for (ComponentDependency component : components) {
            if (component.getDependencies().contains(changedField)) {
                affected.add(
                        component.getComponentType() + " | "
                                + component.getComponentName());
            }
        }
    }
}
