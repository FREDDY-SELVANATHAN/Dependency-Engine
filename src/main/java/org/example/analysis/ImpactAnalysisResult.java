package org.example.analysis;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ImpactAnalysisResult {

    private final String changedField;
    private final Set<String> directImpact;
    private final Set<String> indirectImpact;
    private final List<String> evaluationOrder;
    private final List<List<String>> impactPaths;

    public ImpactAnalysisResult(
            String changedField,
            Set<String> directImpact,
            Set<String> indirectImpact,
            List<String> evaluationOrder,
            List<List<String>> impactPaths) {

        this.changedField = changedField;
        this.directImpact = new LinkedHashSet<>(directImpact);
        this.indirectImpact = new LinkedHashSet<>(indirectImpact);
        this.evaluationOrder = List.copyOf(evaluationOrder);
        this.impactPaths = impactPaths.stream().map(List::copyOf).toList();    }
    public List<List<String>> getImpactPaths(){
        return impactPaths;
    }
    public String getChangedField() {
        return changedField;
    }

    public Set<String> getDirectImpact() {
        return Collections.unmodifiableSet(directImpact);
    }

    public Set<String> getIndirectImpact() {
        return Collections.unmodifiableSet(indirectImpact);
    }

    public List<String> getEvaluationOrder() {
        return evaluationOrder;
    }

    @Override
    public String toString() {
        return "ImpactAnalysisResult{" +
                "changedField='" + changedField + '\'' +
                ", directImpact=" + directImpact +
                ", indirectImpact=" + indirectImpact +
                ", evaluationOrder=" + evaluationOrder +
                ", impactPaths=" + impactPaths +
                '}';
    }
}