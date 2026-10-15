package org.example.schema;

public class SchemaChange {
    private final String type;
    private final String field;
    private final String details;

    public SchemaChange(String type, String field, String details) {
        this.type = type;
        this.field = field;
        this.details = details;
    }

    public String getType() {
        return type;
    }

    public String getField() {
        return field;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return type + " | " + field + " | " + details;
    }
}
