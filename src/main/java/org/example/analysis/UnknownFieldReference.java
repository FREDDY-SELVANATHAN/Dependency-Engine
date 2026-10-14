
package org.example.analysis;

public class UnknownFieldReference {

    private final String sourceField;
    private final String referencedField;
    private final String formula;

    public UnknownFieldReference(
            String sourceField,
            String referencedField,
            String formula) {
        this.sourceField = sourceField;
        this.referencedField = referencedField;
        this.formula = formula;
    }

    public String getSourceField() {
        return sourceField;
    }

    public String getReferencedField() {
        return referencedField;
    }

    public String getFormula() {
        return formula;
    }

    @Override
    public String toString() {
        return "UnknownFieldReference{" +
                "sourceField='" + sourceField + '\'' +
                ", referencedField='" + referencedField + '\'' +
                ", formula='" + formula + '\'' +
                '}';
    }
}
