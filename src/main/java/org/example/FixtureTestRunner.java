package org.example;

import java.io.File;

public class FixtureTestRunner {

    public static void main(String[] args) {

        AppLoader loader = new AppLoader();

        File folder = new File("fixtures");
        File[] files = folder.listFiles();

        if (files == null) {
            System.out.println("Fixtures folder not found!");
            return;
        }

        for (File file : files) {

            if (!file.getName().endsWith(".json")) {
                continue;
            }

            System.out.println("Testing: " + file.getName());

            try {

                App app = loader.load(file.getPath());

                System.out.println("PASS - Loaded successfully");
                System.out.println("App: " + app.getName());

            } catch (RuntimeException e) {

                System.out.println("ERROR - " + e.getMessage());
            }

            System.out.println("-----------------------------");
        }
    }
}