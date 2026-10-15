package org.example.analysis;

import org.example.model.App;
import org.example.model.Field;
import org.example.model.Form;
import org.example.model.Report;
import org.example.model.Validation;
import org.example.model.Workflow;
import org.example.parser.DependencyAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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

        Map<String, Form> formsByName = buildFormsMap(app);

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
        return resolveReferenceForm(
                reference, currentForm, formsByName) != null;
    }

    private Field resolveReferenceForm(
            String reference,
            Form currentForm,
            Map<String, Form> formsByName) {

        if (reference == null || reference.isBlank()
                || currentForm == null) {
            return null;
        }

        String[] parts = reference.split("\\.", -1);

        for (String part : parts) {
            if (part.isBlank()) {
                return null;
            }
        }

        Form targetForm = currentForm;
        int index = 0;

        if (parts[0].equals(currentForm.getName())) {
            index = 1;
        } else if (findField(currentForm, parts[0]) != null) {
            index = 0;
        } else if (formsByName.containsKey(parts[0])) {
            targetForm = formsByName.get(parts[0]);
            index = 1;
        } else {
            return null;
        }

        if (index >= parts.length) {
            return null;
        }

        Field currentField = findField(targetForm, parts[index]);

        if (currentField == null) {
            return null;
        }

        index++;

        while (index < parts.length) {
            String relatedFormName = getRelatedFormName(currentField);

            if (relatedFormName == null) {
                return null;
            }

            targetForm = formsByName.get(relatedFormName);

            if (targetForm == null) {
                return null;
            }

            currentField = findField(targetForm, parts[index]);

            if (currentField == null) {
                return null;
            }

            index++;
        }

        return currentField;
    }

    private String getRelatedFormName(Field field) {
        if (field == null) {
            return null;
        }

        if (field.getLookup() != null
                && !field.getLookup().isBlank()) {
            return field.getLookup();
        }

        if (field.getSubform() != null
                && !field.getSubform().isBlank()) {
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

    private Map<String, Form> buildFormsMap(App app) {
        Map<String, Form> formsByName = new HashMap<>();

        if (app == null || app.getForms() == null) {
            return formsByName;
        }

        for (Form form : app.getForms()) {
            if (form != null
                    && form.getName() != null
                    && !form.getName().isBlank()) {
                formsByName.put(form.getName(), form);
            }
        }

        return formsByName;
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

        Map<String, Form> formsByName = buildFormsMap(app);

        // Reports
        if (app.getReports() != null) {
            for (Report report : app.getReports()) {
                if (report == null) {
                    continue;
                }

                String name = report.getName() == null
                        ? "<unnamed>"
                        : report.getName();

                String formName = report.getForm();

                addComponentExpression(
                        results, "REPORT", name, "filter",
                        report.getFilter(), formName, app, formsByName);

                addComponentExpression(
                        results, "REPORT", name, "sort",
                        sortExpression(report.getSort()),
                        formName, app, formsByName);

                if (report.getColumns() != null) {
                    for (String column : report.getColumns()) {
                        addComponentExpression(
                                results, "REPORT", name, "column",
                                column, formName, app, formsByName);
                    }
                }
            }
        }

        // Validations
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
                            results,
                            "VALIDATION",
                            formName + "." + name,
                            "condition",
                            validation.getCondition(),
                            form.getName(),
                            app,
                            formsByName
                    );
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

                String formName = workflow.getForm();

                addComponentExpression(
                        results, "WORKFLOW", name, "condition",
                        workflow.getCondition(), formName, app, formsByName);

                addComponentExpression(
                        results, "WORKFLOW", name, "action",
                        workflow.getAction(), formName, app, formsByName);
            }
        }

        return results;
    }

    private void addComponentExpression(
            List<ComponentDependency> results,
            String type,
            String name,
            String expressionType,
            String expression,
            String formName,
            App app,
            Map<String, Form> formsByName) {

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
                return;
            }
        }

        // Keep original names for compatibility and add qualified names
        // when the owning form is known.
        List<String> qualifiedDependencies = new ArrayList<>();

        for (String dependency : dependencies) {
            qualifiedDependencies.add(dependency);

            if (formName != null && !formName.isBlank()) {
                String qualified = qualifyComponentReference(
                        dependency, formName, formsByName);

                if (qualified != null
                        && !qualifiedDependencies.contains(qualified)) {
                    qualifiedDependencies.add(qualified);
                }
            }
        }

        results.add(new ComponentDependency(
                type,
                name + "." + expressionType,
                expression,
                new ArrayList<>(
                        new LinkedHashSet<>(qualifiedDependencies))
        ));
    }

    private String qualifyComponentReference(
            String reference,
            String formName,
            Map<String, Form> formsByName) {

        if (reference == null || reference.isBlank()) {
            return reference;
        }

        Form currentForm = formsByName.get(formName);

        if (currentForm == null) {
            return reference;
        }

        String[] parts = reference.split("\\.", -1);

        for (String part : parts) {
            if (part.isBlank()) {
                return reference;
            }
        }

        Form targetForm = currentForm;
        int index = 0;

        if (parts[0].equals(currentForm.getName())) {
            index = 1;
        } else if (findField(currentForm, parts[0]) != null) {
            index = 0;
        } else if (formsByName.containsKey(parts[0])) {
            targetForm = formsByName.get(parts[0]);
            index = 1;
        } else {
            return reference;
        }

        if (index >= parts.length) {
            return reference;
        }

        Field currentField = findField(targetForm, parts[index]);

        if (currentField == null) {
            return reference;
        }

        index++;

        while (index < parts.length) {
            String relatedFormName = getRelatedFormName(currentField);

            if (relatedFormName == null) {
                return reference;
            }

            targetForm = formsByName.get(relatedFormName);

            if (targetForm == null) {
                return reference;
            }

            currentField = findField(targetForm, parts[index]);

            if (currentField == null) {
                return reference;
            }

            index++;
        }

        return qualify(targetForm.getName(), currentField.getName());
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
