package org.example;

public class DependencyGraphTest {

    public static void main(String[] args) throws Exception {

        AppLoader loader = new AppLoader();

        App app = loader.load("app.json");

        DependencyAnalyzer analyzer = new DependencyAnalyzer();

        DependencyGraph graph = analyzer.buildGraph(app);

        System.out.println("=== DEPENDENCY GRAPH ===");

        graph.printGraph();

        System.out.println();

        System.out.println("=== DEPENDENTS ===");

        System.out.println("SubTotal affects: "
                + graph.getDependents("SubTotal"));

        System.out.println("Discount affects: "
                + graph.getDependents("Discount"));

        System.out.println("Qty affects: "
                + graph.getDependents("Qty"));

        System.out.println();

        System.out.println("=== FULL IMPACT ===");

        System.out.println("Changing SubTotal affects: "
                + graph.getAllDependents("SubTotal"));

        System.out.println("Changing Qty affects: "
                + graph.getAllDependents("Qty"));

        System.out.println();

        System.out.println("=== CYCLE CHECK ===");

        System.out.println("Graph contains cycle: "
                + graph.hasCycle());

        DependencyGraph cycleGraph = new DependencyGraph();

        cycleGraph.addDependency("A", "B");
        cycleGraph.addDependency("B", "C");
        cycleGraph.addDependency("C", "A");

        System.out.println("Test cycle: "
                + cycleGraph.hasCycle());

        System.out.println();

        System.out.println("=== EVALUATION ORDER ===");

        System.out.println(graph.getEvaluationOrder());
    }
}