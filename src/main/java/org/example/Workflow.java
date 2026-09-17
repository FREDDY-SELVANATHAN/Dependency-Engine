package org.example;

public class Workflow {
    private String name;
    private String action;
    private String condition;

    public String getName() {
        return name;
    }
    public String getAction() {
        return action;
    }
    public String getCondition() {
        return condition;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public void setCondition(String condition) {
        this.condition = condition;
    }
}
