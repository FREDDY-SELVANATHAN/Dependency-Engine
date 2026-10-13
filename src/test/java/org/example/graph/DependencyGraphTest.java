package org.example.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyGraphTest {

    @Test
    void shouldAddDependencyBetweenTwoNodes() {
        DependencyGraph graph = new DependencyGraph();

        graph.addDependency("Total", "Discount");

        assertTrue(graph.getDependencies("Total").contains("Discount"));
    }

    @Test
    void shouldFindDirectDependents() {
        DependencyGraph graph = new DependencyGraph();

        graph.addDependency("Total", "Discount");

        assertTrue(graph.getDependents("Discount").contains("Total"));
    }

    @Test
    void shouldFindTransitiveDependents() {
        DependencyGraph graph = new DependencyGraph();

        graph.addDependency("B", "A");
        graph.addDependency("C", "B");
        graph.addDependency("D", "C");

        assertTrue(graph.getAllDependents("A").containsAll(
                java.util.List.of("B", "C", "D")
        ));
    }

    @Test
    void shouldDetectCycle() {
        DependencyGraph graph = new DependencyGraph();

        graph.addDependency("B", "A");
        graph.addDependency("A", "B");

        assertTrue(graph.hasCycle());
    }
    @Test
    void shouldNotAddDuplicateDependencies() {
        DependencyGraph graph = new DependencyGraph();

        graph.addDependency("Total", "Discount");
        graph.addDependency("Total", "Discount");

        assertEquals(1, graph.getDependencies("Total").stream()
                .filter(name -> name.equals("Discount"))
                .count());
    }

    @Test
    void shouldHandleUnknownField() {
        DependencyGraph graph = new DependencyGraph();

        assertTrue(graph.getDependents("Unknown").isEmpty());
        assertTrue(graph.getAllDependents("Unknown").isEmpty());
    }

    @Test
    void shouldEvaluateDependenciesInCorrectOrder() {
        DependencyGraph graph = new DependencyGraph();

        graph.addDependency("B", "A");
        graph.addDependency("C", "B");

        java.util.List<String> order = graph.getEvaluationOrder();

        assertTrue(order.indexOf("A") < order.indexOf("B"));
        assertTrue(order.indexOf("B") < order.indexOf("C"));
    }

    @Test
    void shouldRejectEvaluationOrderWhenCycleExists() {
        DependencyGraph graph = new DependencyGraph();

        graph.addDependency("B", "A");
        graph.addDependency("A", "B");

        assertThrows(IllegalStateException.class,
                graph::getEvaluationOrder);
    }
}