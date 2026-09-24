package org.example;

import java.util.List;

public class AppDependencyAnalyzerTest {

    public static void main(String[] args) {

        AppLoader loader = new AppLoader();

        App app = loader.load("app.json");

        AppDependencyAnalyzer analyzer =
                new AppDependencyAnalyzer();

        List<FieldDependency> results =
                analyzer.analyze(app);

        for (FieldDependency dependency : results) {

            System.out.println(
                    "Field: " + dependency.getFieldName()
            );

            System.out.println(
                    "Formula: " + dependency.getFormula()
            );

            System.out.println(
                    "Depends on: " + dependency.getDependencies()
            );

            System.out.println();
        }
    }
}