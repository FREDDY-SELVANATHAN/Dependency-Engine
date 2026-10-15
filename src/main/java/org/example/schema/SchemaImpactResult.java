package org.example.schema;

import org.example.analysis.ImpactAnalysisResult;

import java.util.LinkedHashSet;
import java.util.Set;

public class SchemaImpactResult {

    private final SchemaChange change;
    private final ImpactAnalysisResult impact;
    private final Set<String> affectedComponents;

    public SchemaImpactResult(
            SchemaChange change,
            ImpactAnalysisResult impact,
            Set<String> affectedComponents) {

        this.change = change;
        this.impact = impact;
        this.affectedComponents =
                new LinkedHashSet<>(affectedComponents);
    }

    public SchemaChange getChange() {
        return change;
    }

    public ImpactAnalysisResult getImpact() {
        return impact;
    }

    public Set<String> getAffectedComponents() {
        return java.util.Collections.unmodifiableSet(
                affectedComponents);
    }

    @Override
    public String toString() {
        return "Schema Change: " + change
                + "\nDirect formula impact: "
                + impact.getDirectImpact()
                + "\nIndirect formula impact: "
                + impact.getIndirectImpact()
                + "\nAffected components: "
                + affectedComponents;
    }
}
