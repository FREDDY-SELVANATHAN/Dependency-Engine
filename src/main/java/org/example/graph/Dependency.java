package org.example.graph;

import java.util.Objects;

public class Dependency {

    private final GraphNode source;
    private final GraphNode target;

    public Dependency(GraphNode source, GraphNode target) {
        this.source = source;
        this.target = target;
    }

    public GraphNode getSource() {
        return source;
    }

    public GraphNode getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return source.getName() + " -> " + target.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Dependency)) {
            return false;
        }

        Dependency other = (Dependency) obj;

        return Objects.equals(source, other.source)
                && Objects.equals(target, other.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }
}