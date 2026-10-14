
package org.example.analysis;

import org.example.model.App;
import org.example.model.Field;
import org.example.model.Form;
import org.example.parser.DependencyAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.model.Report;
import org.example.model.Validation;
import org.example.model.Workflow;
import java.util.LinkedHashSet;

public class AppDependencyAnalyzer {

    private final DependencyAnalyzer dependencyAnalyzer;

    public AppDependencyAnalyzer() {
        this.dependencyAnalyzer = new DependencyAnalyzer();
    }

    public List<FieldDependency> analyze(App app) {

        List<FieldDependency> results = new ArrayList<>();

        if (app == null || app.getForms() == null) {
            return results;
        }

        for (Form form : app.getForms()) {
            if (form == null || form.getFields() == null) {
                continue;
            }

            for (Field field : form.getFields()) {
                if (field == null
                        || field.getFormula() == null
                        || field.getFormula().isBlank()) {
                    continue;
                }

                List<String> dependencies =
                        dependencyAnalyzer.analyzeField(field);

                results.add(new FieldDependency(
                        qualify(form.getName(), field.getName()),
                        field.getFormula(),
                        dependencies
                ));
            }
        }

        return results;
    }

    public List<UnknownFieldReference> findUnknownReferences(App app) {

        List<UnknownFieldReference> issues = new ArrayList<>();

        if (app == null || app.getForms() == null) {
            return issues;
        }

        Map<String, Form> formsByName = new HashMap<>();

        for (Form form : app.getForms()) {
            if (form != null
                    && form.getName() != null
                    && !form.getName().isBlank()) {
                formsByName.put(form.getName(), form);
            }
        }

        for (Form form : app.getForms()) {
            if (form == null || form.getFields() == null) {
                continue;
            }

            for (Field field : form.getFields()) {
                if (field == null
                        || field.getFormula() == null
                        || field.getFormula().isBlank()) {
                    continue;
                }

                List<String> references;

                try {
                    references = dependencyAnalyzer.analyzeField(field);
                } catch (IllegalArgumentException exception) {
                    // Syntax errors are reported separately by findFormulaErrors().
                    continue;
                }

                for (String reference : references) {
                    if (!resolvesReference(reference, form, formsByName)) {
                        issues.add(new UnknownFieldReference(
                                qualify(form.getName(), field.getName()),
                                reference,
                                field.getFormula()
                        ));
                    }
                }
            }
        }

        return issues;
    }

    private boolean resolvesReference(
            String reference,
            Form currentForm,
            Map<String, Form> formsByName) {

        if (reference == null || reference.isBlank()) {
            return false;
        }

        String[] parts = reference.split("\\.", -1);

        for (String part : parts) {
            if (part.isBlank()) {
                return false;
            }
        }

        Form targetForm = currentForm;
        int index = 0;

        // Support explicitly form-qualified paths such as Order.Customer.Name.
        if (parts[0].equals(currentForm.getName())) {
            index = 1;
        } else if (findField(currentForm, parts[0]) != null) {
            // Prefer a field in the current form.
            // Example: Order.Customer.DiscountPct.
            index = 0;
        } else if (formsByName.containsKey(parts[0])) {
            // Preserve explicit cross-form references such as Customer.DiscountPct.
            targetForm = formsByName.get(parts[0]);
            index = 1;
        } else {
            return false;
        }

        if (index >= parts.length) {
            return false;
        }

        Field currentField = findField(targetForm, parts[index]);

        if (currentField == null) {
            return false;
        }

        index++;

        while (index < parts.length) {

            // A remaining path segment must follow a lookup or subform.
            String relatedFormName = getRelatedFormName(currentField);

            if (relatedFormName == null) {
                return false;
            }

            targetForm = formsByName.get(relatedFormName);

            if (targetForm == null) {
                return false;
            }

            currentField = findField(targetForm, parts[index]);

            if (currentField == null) {
                return false;
            }

            index++;
        }

        return true;
    }

    private String getRelatedFormName(Field field) {

        if (field == null) {
            return null;
        }

        if (field.getLookup() != null && !field.getLookup().isBlank()) {
            return field.getLookup();
        }

        if (field.getSubform() != null && !field.getSubform().isBlank()) {
            return field.getSubform();
        }

        return null;
    }

    private Field findField(Form form, String fieldName) {

        if (form == null
                || form.getFields() == null
                || fieldName == null) {
            return null;
        }

        for (Field field : form.getFields()) {
            if (field != null && fieldName.equals(field.getName())) {
                return field;
            }
        }

        return null;
    }

    private String qualify(String formName, String fieldName) {
        if (formName == null || formName.isBlank()) {
            return fieldName;
        }

        return formName + "." + fieldName;
    }

    public List<FormulaValidationError> findFormulaErrors(App app) {

        List<FormulaValidationError> errors = new ArrayList<>();

        if (app == null || app.getForms() == null) {
            return errors;
        }

        for (Form form : app.getForms()) {
            if (form == null || form.getFields() == null) {
                continue;
            }

            for (Field field : form.getFields()) {
                if (field == null
                        || field.getFormula() == null
                        || field.getFormula().isBlank()) {
                    continue;
                }

                try {
                    dependencyAnalyzer.analyzeField(field);
                } catch (IllegalArgumentException exception) {

                    String formName = form.getName() == null
                            ? "<unknown form>"
                            : form.getName();

                    String fieldName = field.getName() == null
                            ? "<unknown field>"
                            : field.getName();

                    errors.add(new FormulaValidationError(
                            formName + "." + fieldName,
                            field.getFormula(),
                            exception.getMessage() == null
                                    ? "Invalid formula"
                                    : exception.getMessage()
                    ));
                }
            }
        }

        return errors;
    }

    public List<ComponentDependency> analyzeComponents(App app) {

        List<ComponentDependency> results = new ArrayList<>();

        if (app == null) {
            return results;
        }

        // Reports
        if (app.getReports() != null) {
            for (Report report : app.getReports()) {
                if (report == null) {
                    continue;
                }

                String name = report.getName() == null
                        ? "<unnamed>"
                        : report.getName();

                addComponentExpression(
                        results, "REPORT", name, "filter",
                        report.getFilter());

                addComponentExpression(
                        results, "REPORT", name, "sort",
                        sortExpression(report.getSort()));

                if (report.getColumns() != null) {
                    for (String column : report.getColumns()) {
                        addComponentExpression(
                                results, "REPORT", name, "column",
                                column);
                    }
                }
            }
        }

        // Form validations
        if (app.getForms() != null) {
            for (Form form : app.getForms()) {
                if (form == null || form.getValidations() == null) {
                    continue;
                }

                String formName = form.getName() == null
                        ? "<unknown form>"
                        : form.getName();

                for (Validation validation : form.getValidations()) {
                    if (validation == null) {
                        continue;
                    }

                    String name = validation.getName() == null
                            ? "<unnamed>"
                            : validation.getName();

                    addComponentExpression(
                            results, "VALIDATION",
                            formName + "." + name,
                            "condition",
                            validation.getCondition());
                }
            }
        }

        // Workflows
        if (app.getWorkflows() != null) {
            for (Workflow workflow : app.getWorkflows()) {
                if (workflow == null) {
                    continue;
                }

                String name = workflow.getName() == null
                        ? "<unnamed>"
                        : workflow.getName();

                addComponentExpression(
                        results, "WORKFLOW", name, "condition",
                        workflow.getCondition());

                addComponentExpression(
                        results, "WORKFLOW", name, "action",
                        workflow.getAction());
            }
        }

        return results;
    }

    private void addComponentExpression(
            List<ComponentDependency> results,
            String type,
            String name,
            String expressionType,
            String expression) {

        if (expression == null || expression.isBlank()) {
            return;
        }

        List<String> dependencies;

        if ("column".equals(expressionType)) {
            dependencies = List.of(expression.trim());
        } else {
            try {
                dependencies = dependencyAnalyzer.analyze(expression);
            } catch (IllegalArgumentException exception) {
                // Do not silently interpret malformed expressions as valid.
                // They are omitted here and will be handled by validation.
                return;
            }
        }

        results.add(new ComponentDependency(
                type,
                name + "." + expressionType,
                expression,
                new ArrayList<>(new LinkedHashSet<>(dependencies))
        ));
    }

    private String sortExpression(String sort) {
        if (sort == null || sort.isBlank()) {
            return null;
        }

        String value = sort.trim();
        String[] parts = value.split("\\s+");

        if (parts.length == 2
                && ("asc".equalsIgnoreCase(parts[1])
                || "desc".equalsIgnoreCase(parts[1]))) {
            return parts[0];
        }

        return value;
    }

}
