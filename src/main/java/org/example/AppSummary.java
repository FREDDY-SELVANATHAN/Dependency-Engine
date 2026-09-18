package org.example;

public class AppSummary {

    public void printSummary(App app) {

        int formCount = app.getForms().size();
        int fieldCount = 0;
        int validationCount = 0;

        for (Form form : app.getForms()) {

            fieldCount += form.getFields().size();

            if (form.getValidations() != null) {
                validationCount += form.getValidations().size();
            }
        }

        int reportCount = app.getReports() == null
                ? 0
                : app.getReports().size();

        int workflowCount = app.getWorkflows() == null
                ? 0
                : app.getWorkflows().size();

        System.out.println(
                app.getName() + ": "
                        + formCount + " forms, "
                        + fieldCount + " fields, "
                        + validationCount + " validations, "
                        + reportCount + " reports, "
                        + workflowCount + " workflow(s)"
        );
    }
}