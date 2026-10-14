
package org.example.analysis;

import org.example.graph.DependencyGraph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImpactAnalyzerTest {

    @Test
    void shouldIdentifyDirectImpact() {
        DependencyGraph graph = new DependencyGraph();
        graph.addDependency("Total", "Quantity");
        graph.addDependency("Total", "Price");

        ImpactAnalyzer analyzer = new ImpactAnalyzer(graph);

        ImpactAnalysisResult result = analyzer.analyze("Quantity");

        assertEquals(
                java.util.Set.of("Total"),
                result.getDirectImpact()
        );
        assertTrue(result.getIndirectImpact().isEmpty());
    }

    @Test
    void shouldIdentifyTransitiveImpactAcrossMultipleLevels() {
        DependencyGraph graph = new DependencyGraph();
        graph.addDependency("Total", "Quantity");
        graph.addDependency("FinalAmount", "Total");

        ImpactAnalyzer analyzer = new ImpactAnalyzer(graph);

        ImpactAnalysisResult result = analyzer.analyze("Quantity");

        assertEquals(
                java.util.Set.of("Total"),
                result.getDirectImpact()
        );
        assertEquals(
                java.util.Set.of("FinalAmount"),
                result.getIndirectImpact()
        );
    }

    @Test
    void shouldReportPathsThroughBranchingDependencies() {
        DependencyGraph graph = new DependencyGraph();
        graph.addDependency("Total", "Quantity");
        graph.addDependency("DiscountedTotal", "Quantity");
        graph.addDependency("FinalAmount", "Total");
        graph.addDependency("FinalAmount", "DiscountedTotal");

        ImpactAnalyzer analyzer = new ImpactAnalyzer(graph);

        ImpactAnalysisResult result = analyzer.analyze("Quantity");

        assertEquals(
                java.util.Set.of("Total", "DiscountedTotal"),
                result.getDirectImpact()
        );
        assertTrue(
                result.getIndirectImpact().contains("FinalAmount")
        );

        assertTrue(result.getImpactPaths().contains(
                List.of("Quantity", "Total", "FinalAmount")
        ));
        assertTrue(result.getImpactPaths().contains(
                List.of("Quantity", "DiscountedTotal", "FinalAmount")
        ));
    }

    @Test
    void shouldRejectImpactEvaluationWhenGraphContainsCycle() {
        DependencyGraph graph = new DependencyGraph();
        graph.addDependency("A", "B");
        graph.addDependency("B", "A");

        ImpactAnalyzer analyzer = new ImpactAnalyzer(graph);

        assertThrows(
                IllegalStateException.class,
                () -> analyzer.analyze("A")
        );
    }
}
