package org.example.schema;

import org.example.model.App;
import org.example.model.Field;
import org.example.model.Form;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SchemaChangeAnalyzer {

    public List<SchemaChange> compare(App oldApp, App newApp) {
        Map<String, Field> oldFields = collectFields(oldApp);
        Map<String, Field> newFields = collectFields(newApp);

        List<SchemaChange> changes = new ArrayList<>();

        for (String name : oldFields.keySet()) {
            if (!newFields.containsKey(name)) {
                changes.add(new SchemaChange(
                        "REMOVED", name, "Field was removed"
                ));
            }
        }

        for (String name : newFields.keySet()) {
            Field current = newFields.get(name);
            Field previous = oldFields.get(name);

            if (previous == null) {
                changes.add(new SchemaChange(
                        "ADDED", name, "Field was added"
                ));
                continue;
            }

            checkChange(changes, name, "type",
                    previous.getType(), current.getType());

            checkChange(changes, name, "formula",
                    previous.getFormula(), current.getFormula());

            checkChange(changes, name, "lookup",
                    previous.getLookup(), current.getLookup());

            checkChange(changes, name, "subform",
                    previous.getSubform(), current.getSubform());
        }

        return changes;
    }

    private Map<String, Field> collectFields(App app) {
        Map<String, Field> fields = new LinkedHashMap<>();

        if (app.getForms() == null) {
            return fields;
        }

        for (Form form : app.getForms()) {
            if (form.getFields() == null) {
                continue;
            }

            for (Field field : form.getFields()) {
                fields.put(
                        form.getName() + "." + field.getName(),
                        field
                );
            }
        }

        return fields;
    }

    private void checkChange(
            List<SchemaChange> changes,
            String field,
            String property,
            String oldValue,
            String newValue) {

        if (!Objects.equals(oldValue, newValue)) {
            changes.add(new SchemaChange(
                    "MODIFIED",
                    field,
                    property + " changed from '" + oldValue
                            + "' to '" + newValue + "'"
            ));
        }
    }
}
