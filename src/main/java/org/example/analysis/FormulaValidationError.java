
package org.example.analysis;

public class FormulaValidationError {

    private final String sourceField;
    private final String formula;
    private final String message;

    public FormulaValidationError(
            String sourceField,
            String formula,
            String message) {
        this.sourceField = sourceField;
        this.formula = formula;
        this.message = message;
    }

    public String getSourceField() {
        return sourceField;
    }

    public String getFormula() {
        return formula;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "FormulaValidationError{" +
                "sourceField='" + sourceField + '\'' +
                ", formula='" + formula + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
