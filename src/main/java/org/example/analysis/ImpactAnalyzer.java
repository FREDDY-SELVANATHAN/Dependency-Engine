package org.example.analysis;

import org.example.graph.DependencyGraph;

import java.util.LinkedHashSet;
import java.util.List;
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

        // Remove direct impact to leave only indirect impact.
        Set<String> indirectImpact =
                new LinkedHashSet<>(allImpact);

        indirectImpact.removeAll(directImpact);

        List<String> evaluationOrder =
                graph.getEvaluationOrder();

        List<List<String>> impactPaths =
                graph.getImpactPaths(changedField);

        return new ImpactAnalysisResult(
                changedField,
                directImpact,
                indirectImpact,
                evaluationOrder,
                impactPaths
        );
    }
}