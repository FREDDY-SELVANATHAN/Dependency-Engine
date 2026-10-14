
package org.example.analysis;

import java.util.List;

public class ComponentDependency {

    private final String componentType;
    private final String componentName;
    private final String expression;
    private final List<String> dependencies;

    public ComponentDependency(
            String componentType,
            String componentName,
            String expression,
            List<String> dependencies) {
        this.componentType = componentType;
        this.componentName = componentName;
        this.expression = expression;
        this.dependencies = List.copyOf(dependencies);
    }

    public String getComponentType() {
        return componentType;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getExpression() {
        return expression;
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}
