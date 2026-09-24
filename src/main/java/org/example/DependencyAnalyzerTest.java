package org.example;

import java.util.List;

public class DependencyAnalyzerTest {

    public static void main(String[] args) {

        DependencyAnalyzer analyzer = new DependencyAnalyzer();

        String formula = "Qty * Rate";

        List<String> dependencies = analyzer.analyze(formula);

        System.out.println("Formula: " + formula);
        System.out.println("Dependencies: " + dependencies);
    }
}