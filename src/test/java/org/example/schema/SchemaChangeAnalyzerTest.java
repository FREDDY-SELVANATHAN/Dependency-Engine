package org.example.schema;

import org.example.model.App;
import org.example.model.Field;
import org.example.model.Form;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SchemaChangeAnalyzerTest {

    private final SchemaChangeAnalyzer analyzer =
            new SchemaChangeAnalyzer();

    private App createApp(String fieldName, String type,
                          String formula) {
        Field field = new Field();
        field.setName(fieldName);
        field.setType(type);
        field.setFormula(formula);

        Form form = new Form();
        form.setName("Order");
        form.setFields(List.of(field));

        App app = new App();
        app.setName("TestApp");
        app.setForms(List.of(form));

        return app;
    }

    @Test
    void detectsAddedField() {
        App oldApp = createApp("Total", "Number", null);

        Field total = new Field();
        total.setName("Total");
        total.setType("Number");

        Field discount = new Field();
        discount.setName("Discount");
        discount.setType("Number");

        Form form = new Form();
        form.setName("Order");
        form.setFields(List.of(total, discount));

        App newApp = new App();
        newApp.setName("TestApp");
        newApp.setForms(List.of(form));

        List<SchemaChange> changes =
                analyzer.compare(oldApp, newApp);

        assertTrue(changes.stream().anyMatch(change ->
                change.getType().equals("ADDED")
                        && change.getField().equals("Order.Discount")));
    }

    @Test
    void detectsRemovedField() {
        App oldApp = createApp("Total", "Number", null);
        App newApp = createApp("Discount", "Number", null);

        List<SchemaChange> changes =
                analyzer.compare(oldApp, newApp);

        assertTrue(changes.stream().anyMatch(change ->
                change.getType().equals("REMOVED")
                        && change.getField().equals("Order.Total")));
    }

    @Test
    void detectsModifiedFieldType() {
        App oldApp = createApp("Total", "Number", null);
        App newApp = createApp("Total", "Currency", null);

        List<SchemaChange> changes =
                analyzer.compare(oldApp, newApp);

        assertTrue(changes.stream().anyMatch(change ->
                change.getType().equals("MODIFIED")
                        && change.getField().equals("Order.Total")
                        && change.getDetails().contains("type")));
    }

    @Test
    void detectsModifiedFormula() {
        App oldApp = createApp("Total", "Number", "Price * Qty");
        App newApp = createApp("Total", "Number", "Price * Qty * 2");

        List<SchemaChange> changes =
                analyzer.compare(oldApp, newApp);

        assertTrue(changes.stream().anyMatch(change ->
                change.getType().equals("MODIFIED")
                        && change.getField().equals("Order.Total")
                        && change.getDetails().contains("formula")));
    }

    @Test
    void reportsNoChangesWhenSchemasAreIdentical() {
        App oldApp = createApp("Total", "Number", "Price * Qty");
        App newApp = createApp("Total", "Number", "Price * Qty");

        List<SchemaChange> changes =
                analyzer.compare(oldApp, newApp);

        assertTrue(changes.isEmpty());
    }
}
