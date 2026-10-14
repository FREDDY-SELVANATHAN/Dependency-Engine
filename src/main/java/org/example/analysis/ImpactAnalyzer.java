
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

        Set<String> indirectImpact = new LinkedHashSet<>();
        Set<String> visited = new LinkedHashSet<>();
        Queue<String> queue = new LinkedList<>();

        visited.add(changedField);
        queue.add(changedField);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            for (String dependent : graph.getDependents(current)) {
                if (visited.add(dependent)) {
                    queue.add(dependent);

                    if (!directImpact.contains(dependent)) {
                        indirectImpact.add(dependent);
                    }
                }
            }
        }

        indirectImpact.removeAll(directImpact);

        return new ImpactAnalysisResult(
                changedField,
                directImpact,
                indirectImpact,
                graph.getEvaluationOrder(),
                graph.getImpactPaths(changedField)
        );
    }


}
