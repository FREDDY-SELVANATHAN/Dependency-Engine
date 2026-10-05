package org.example;

import org.example.analysis.ImpactAnalysisResult;
import org.example.analysis.ImpactAnalyzer;
import org.example.graph.DependencyGraph;
import org.example.loader.AppLoader;
import org.example.model.App;
import org.example.parser.DependencyAnalyzer;

import java.util.List;

public class ImpactAnalyzerTest {

    public static void main(String[] args) {

        AppLoader loader = new AppLoader();

        App app = loader.load("app.json");

        DependencyAnalyzer analyzer =
                new DependencyAnalyzer();

        DependencyGraph graph =
                analyzer.buildGraph(app);

        ImpactAnalyzer impactAnalyzer =
                new ImpactAnalyzer(graph);

        ImpactAnalysisResult result =
                impactAnalyzer.analyze("Discount");

        System.out.println("=== IMPACT ANALYSIS ===");

        System.out.println(
                "Changed field: "
                        + result.getChangedField()
        );

        System.out.println(
                "Direct impact: "
                        + result.getDirectImpact()
        );

        System.out.println(
                "Indirect impact: "
                        + result.getIndirectImpact()
        );

        System.out.println(
                "Evaluation order: "
                        + result.getEvaluationOrder()
        );

        System.out.println("Impact paths:");

        for (List<String> path : result.getImpactPaths()) {
            System.out.println(
                    "  " + String.join(" -> ", path)
            );
        }
    }
}