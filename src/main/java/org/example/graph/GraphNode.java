package org.example.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GraphNode {

    private final String name;

    private final List<Dependency> outgoingDependencies;
    private final List<Dependency> incomingDependencies;

    public GraphNode(String name) {
        this.name = name;
        this.outgoingDependencies = new ArrayList<>();
        this.incomingDependencies = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Dependency> getOutgoingDependencies() {
        return outgoingDependencies;
    }

    public List<Dependency> getIncomingDependencies() {
        return incomingDependencies;
    }

    public void addOutgoingDependency(Dependency dependency) {
        outgoingDependencies.add(dependency);
    }

    public void addIncomingDependency(Dependency dependency) {
        incomingDependencies.add(dependency);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof GraphNode)) {
            return false;
        }

        GraphNode other = (GraphNode) obj;

        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}