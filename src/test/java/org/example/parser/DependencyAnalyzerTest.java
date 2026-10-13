package org.example.parser;

import org.example.graph.DependencyGraph;
import org.example.model.App;
import org.example.model.Field;
import org.example.model.Form;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyAnalyzerTest {

    private final DependencyAnalyzer analyzer = new DependencyAnalyzer();

    @Test
    void shouldExtractDependenciesFromFormula() {
        List<String> dependencies =
                analyzer.analyze("SubTotal * Customer.DiscountPct / 100");

        assertEquals(
                List.of("SubTotal", "Customer.DiscountPct"),
                dependencies
        );
    }

    @Test
    void shouldReturnEmptyListForFieldWithoutFormula() {
        Field field = new Field();
        field.setName("Quantity");
        field.setType("number");

        assertTrue(analyzer.analyzeField(field).isEmpty());
    }

    @Test
    void shouldExtractDependenciesFromFieldFormula() {
        Field field = new Field();
        field.setName("Total");
        field.setType("number");
        field.setFormula("Quantity * Price");

        List<String> dependencies = analyzer.analyzeField(field);

        assertEquals(List.of("Quantity", "Price"), dependencies);
    }

    @Test
    void shouldBuildGraphAutomaticallyFromApplication() {
        Field quantity = new Field();
        quantity.setName("Quantity");
        quantity.setType("number");

        Field price = new Field();
        price.setName("Price");
        price.setType("number");

        Field total = new Field();
        total.setName("Total");
        total.setType("number");
        total.setFormula("Quantity * Price");

        Form form = new Form();
        form.setName("Order");
        form.setFields(List.of(quantity, price, total));

        App app = new App();
        app.setForms(List.of(form));

        DependencyGraph graph = analyzer.buildGraph(app);

        assertTrue(graph.getDependencies("Total").contains("Quantity"));
        assertTrue(graph.getDependencies("Total").contains("Price"));
    }
}