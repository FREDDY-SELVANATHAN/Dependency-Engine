package org.example;

import java.util.List;

public class FieldDependency {

    private final String fieldName;
    private final String formula;
    private final List<String> dependencies;

    public FieldDependency(
            String fieldName,
            String formula,
            List<String> dependencies
    ) {
        this.fieldName = fieldName;
        this.formula = formula;
        this.dependencies = dependencies;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFormula() {
        return formula;
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}