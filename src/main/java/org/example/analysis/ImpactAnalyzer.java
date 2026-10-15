
package org.example.analysis;

import org.example.graph.DependencyGraph;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ImpactAnalyzer {

    private final DependencyGraph graph;

    public ImpactAnalyzer(DependencyGraph graph) {
        this.graph = graph;
    }




    public ImpactAnalysisResult analyze(String changedField) {

        Set<String> directImpact =
                new LinkedHashSet<>(
                        graph.getDependents(changedField)
                );

        Set<String> allImpact =
                new LinkedHashSet<>(
                        graph.getAllDependents(changedField)
                );

        Set<String> indirectImpact =
                new LinkedHashSet<>(allImpact);

        indirectImpact.removeAll(directImpact);

        // Remove direct links that are also reachable through
        // another impacted formula.
        for (String direct : new LinkedHashSet<>(directImpact)) {
            Set<String> downstream = graph.getAllDependents(direct);

            for (String candidate : downstream) {
                if (directImpact.contains(candidate)) {
                    indirectImpact.add(candidate);
                }
            }
        }

        directImpact.removeAll(indirectImpact);

        return new ImpactAnalysisResult(
                changedField,
                directImpact,
                indirectImpact,
                graph.getEvaluationOrder(),
                graph.getImpactPaths(changedField)
        );
    }



}
