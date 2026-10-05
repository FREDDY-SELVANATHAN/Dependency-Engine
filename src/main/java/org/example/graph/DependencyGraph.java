package org.example.graph;

import java.util.*;

public class DependencyGraph {

    private final Set<GraphNode> nodes;
    private final Set<Dependency> edges;

    public DependencyGraph() {
        nodes = new LinkedHashSet<>();
        edges = new LinkedHashSet<>();
    }

    // --------------------------------------------------
    // NODE MANAGEMENT
    // --------------------------------------------------

    public GraphNode addNode(String name) {

        for (GraphNode node : nodes) {
            if (node.getName().equals(name)) {
                return node;
            }
        }

        GraphNode node = new GraphNode(name);
        nodes.add(node);

        return node;
    }

    public GraphNode getNode(String name) {

        for (GraphNode node : nodes) {
            if (node.getName().equals(name)) {
                return node;
            }
        }

        return null;
    }

    public Set<GraphNode> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public Set<Dependency> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    // --------------------------------------------------
    // EDGE / DEPENDENCY MANAGEMENT
    // --------------------------------------------------

    public void addDependency(String sourceName, String targetName) {

        GraphNode source = addNode(sourceName);
        GraphNode target = addNode(targetName);

        Dependency dependency =
                new Dependency(source, target);

        // Prevent duplicate dependency
        if (edges.add(dependency)) {

            source.addOutgoingDependency(dependency);
            target.addIncomingDependency(dependency);
        }
    }

    // --------------------------------------------------
    // DIRECT DEPENDENCIES
    // --------------------------------------------------

    public Set<String> getDependencies(String sourceName) {

        GraphNode source = getNode(sourceName);

        if (source == null) {
            return Collections.emptySet();
        }

        Set<String> dependencies = new LinkedHashSet<>();

        for (Dependency dependency :
                source.getOutgoingDependencies()) {

            dependencies.add(
                    dependency.getTarget().getName()
            );
        }

        return dependencies;
    }

    // --------------------------------------------------
    // DIRECT DEPENDENTS
    // --------------------------------------------------

    public Set<String> getDependents(String fieldName) {

        GraphNode node = getNode(fieldName);

        if (node == null) {
            return Collections.emptySet();
        }

        Set<String> dependents = new LinkedHashSet<>();

        for (Dependency dependency :
                node.getIncomingDependencies()) {

            dependents.add(
                    dependency.getSource().getName()
            );
        }

        return dependents;
    }

    // --------------------------------------------------
    // FULL / INDIRECT IMPACT
    // --------------------------------------------------

    public Set<String> getAllDependents(String fieldName) {

        GraphNode start = getNode(fieldName);

        if (start == null) {
            return Collections.emptySet();
        }

        Set<String> result = new LinkedHashSet<>();

        Queue<GraphNode> queue = new LinkedList<>();

        queue.add(start);

        while (!queue.isEmpty()) {

            GraphNode current = queue.poll();

            for (Dependency dependency :
                    current.getIncomingDependencies()) {

                GraphNode dependent =
                        dependency.getSource();

                if (result.add(dependent.getName())) {
                    queue.add(dependent);
                }
            }
        }

        return result;
    }

    // --------------------------------------------------
    // CYCLE DETECTION
    // --------------------------------------------------

    public boolean hasCycle() {

        Set<GraphNode> visited = new HashSet<>();
        Set<GraphNode> recursionStack = new HashSet<>();

        for (GraphNode node : nodes) {

            if (hasCycle(
                    node,
                    visited,
                    recursionStack)) {

                return true;
            }
        }

        return false;
    }

    private boolean hasCycle(
            GraphNode node,
            Set<GraphNode> visited,
            Set<GraphNode> recursionStack) {

        if (recursionStack.contains(node)) {
            return true;
        }

        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        recursionStack.add(node);

        for (Dependency dependency :
                node.getOutgoingDependencies()) {

            GraphNode target =
                    dependency.getTarget();

            if (hasCycle(
                    target,
                    visited,
                    recursionStack)) {

                return true;
            }
        }

        recursionStack.remove(node);

        return false;
    }

    // --------------------------------------------------
    // EVALUATION ORDER
    // --------------------------------------------------

    public List<String> getEvaluationOrder() {

        /*
         * Direction:
         *
         * Total -> Discount
         *
         * means:
         * Total depends on Discount.
         *
         * Therefore Discount must be evaluated first.
         */

        Map<GraphNode, Integer> remainingDependencies =
                new LinkedHashMap<>();

        for (GraphNode node : nodes) {

            remainingDependencies.put(
                    node,
                    node.getOutgoingDependencies().size()
            );
        }

        Queue<GraphNode> queue =
                new LinkedList<>();

        // Nodes with no dependencies can be evaluated first.
        for (GraphNode node : nodes) {

            if (remainingDependencies.get(node) == 0) {
                queue.add(node);
            }
        }

        List<String> order = new ArrayList<>();

        while (!queue.isEmpty()) {

            GraphNode current = queue.poll();

            order.add(current.getName());

            /*
             * If current is a dependency of another node,
             * that other node has one fewer dependency left.
             */

            for (Dependency dependency :
                    current.getIncomingDependencies()) {

                GraphNode dependent =
                        dependency.getSource();

                int remaining =
                        remainingDependencies.get(dependent) - 1;

                remainingDependencies.put(
                        dependent,
                        remaining
                );

                if (remaining == 0) {
                    queue.add(dependent);
                }
            }
        }

        if (order.size() != nodes.size()) {

            throw new IllegalStateException(
                    "Cannot determine evaluation order: graph contains a cycle"
            );
        }

        return order;
    }

    public List<List<String>> getImpactPaths(String startName) {

        GraphNode start = getNode(startName);

        if (start == null) {
            return Collections.emptyList();
        }

        List<List<String>> paths = new ArrayList<>();

        List<String> currentPath = new ArrayList<>();
        currentPath.add(start.getName());

        findImpactPaths(start, currentPath, paths);

        return paths;
    }

    private void findImpactPaths(
            GraphNode current,
            List<String> currentPath,
            List<List<String>> paths) {

        if (current.getIncomingDependencies().isEmpty()) {
            paths.add(new ArrayList<>(currentPath));
            return;
        }

        for (Dependency dependency :
                current.getIncomingDependencies()) {

            GraphNode dependent =
                    dependency.getSource();

            if (currentPath.contains(dependent.getName())) {
                continue;
            }

            currentPath.add(dependent.getName());

            findImpactPaths(
                    dependent,
                    currentPath,
                    paths
            );

            currentPath.remove(
                    currentPath.size() - 1
            );
        }
    }

    // --------------------------------------------------
    // GRAPH DISPLAY
    // --------------------------------------------------

    public void printGraph() {

        for (GraphNode node : nodes) {

            if (!node.getOutgoingDependencies().isEmpty()) {

                System.out.print(
                        node.getName() + " -> ["
                );

                List<Dependency> dependencies =
                        node.getOutgoingDependencies();

                for (int i = 0;
                     i < dependencies.size();
                     i++) {

                    System.out.print(
                            dependencies
                                    .get(i)
                                    .getTarget()
                                    .getName()
                    );

                    if (i < dependencies.size() - 1) {
                        System.out.print(", ");
                    }
                }

                System.out.println("]");
            }
        }
    }
}