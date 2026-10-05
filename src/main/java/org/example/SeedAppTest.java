package org.example;

import org.example.loader.AppLoader;
import org.example.model.App;
import org.example.summary.AppSummary;

public class SeedAppTest {

    public static void main(String[] args) {

        AppLoader loader = new AppLoader();

        App app = loader.load("fixtures/unknown-property.json");

        AppSummary summary = new AppSummary();

        summary.printSummary(app);
    }
}