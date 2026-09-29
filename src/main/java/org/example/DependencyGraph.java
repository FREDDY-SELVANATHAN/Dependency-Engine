package org.example;

import java.util.*;

public class DependencyGraph {

    private final Map<String, Set<String>> dependencies = new HashMap<>();

    public void addDependency(String source, String target) {
        dependencies
                .computeIfAbsent(source, k -> new LinkedHashSet<>())
                .add(target);
    }

    public Set<String> getDependencies(String source) {
        return dependencies.getOrDefault(source, Collections.emptySet());
    }

    public Map<String, Set<String>> getAllDependencies() {
        return Collections.unmodifiableMap(dependencies);
    }

    public void printGraph() {
        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            System.out.println(
                    entry.getKey() + " -> " + entry.getValue()
            );
        }
    }
    public Set<String> getDependents(String field) {

        Set<String> dependents = new LinkedHashSet<>();

        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {

            if (entry.getValue().contains(field)) {
                dependents.add(entry.getKey());
            }
        }

        return dependents;
    }

    public Set<String> getAllDependents(String field) {

        Set<String> result = new LinkedHashSet<>();
        Queue<String> queue = new LinkedList<>();

        queue.add(field);

        while (!queue.isEmpty()) {

            String current = queue.poll();

            for (String dependent : getDependents(current)) {

                if (result.add(dependent)) {
                    queue.add(dependent);
                }
            }
        }

        return result;
    }

    public boolean hasCycle() {

        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String node : dependencies.keySet()) {

            if (hasCycle(node, visited, recursionStack)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasCycle(
            String node,
            Set<String> visited,
            Set<String> recursionStack) {

        if (recursionStack.contains(node)) {
            return true;
        }

        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        recursionStack.add(node);

        for (String dependency : getDependencies(node)) {

            if (hasCycle(dependency, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(node);

        return false;
    }

    public List<String> getEvaluationOrder() {

        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, Set<String>> reverseGraph = new HashMap<>();

        for (String node : dependencies.keySet()) {

            inDegree.putIfAbsent(node, 0);

            for (String dependency : dependencies.get(node)) {

                inDegree.putIfAbsent(dependency, 0);

                reverseGraph
                        .computeIfAbsent(dependency, k -> new LinkedHashSet<>())
                        .add(node);

                inDegree.put(
                        node,
                        inDegree.get(node) + 1
                );
            }
        }

        Queue<String> queue = new LinkedList<>();

        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {

            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> order = new ArrayList<>();

        while (!queue.isEmpty()) {

            String current = queue.poll();

            order.add(current);

            for (String dependent :
                    reverseGraph.getOrDefault(
                            current,
                            Collections.emptySet())) {

                int newDegree = inDegree.get(dependent) - 1;

                inDegree.put(dependent, newDegree);

                if (newDegree == 0) {
                    queue.add(dependent);
                }
            }
        }

        if (order.size() != inDegree.size()) {

            throw new IllegalStateException(
                    "Cannot determine evaluation order: graph contains a cycle"
            );
        }

        return order;
    }
}