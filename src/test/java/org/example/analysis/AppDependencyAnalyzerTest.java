
package org.example.analysis;

import org.example.model.App;
import org.example.model.Field;
import org.example.model.Form;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppDependencyAnalyzerTest {

    private final AppDependencyAnalyzer analyzer =
            new AppDependencyAnalyzer();

    private App createApp(String formula) {
        Field quantity = new Field();
        quantity.setName("Quantity");
        quantity.setType("number");

        Field price = new Field();
        price.setName("Price");
        price.setType("number");

        Field total = new Field();
        total.setName("Total");
        total.setType("number");
        total.setFormula(formula);

        Form order = new Form();
        order.setName("Order");
        order.setFields(List.of(quantity, price, total));

        App app = new App();
        app.setForms(List.of(order));

        return app;
    }

    private App createAppWithCustomer(String formula) {
        App app = createApp(formula);

        Field discountPct = new Field();
        discountPct.setName("DiscountPct");
        discountPct.setType("number");

        Form customer = new Form();
        customer.setName("Customer");
        customer.setFields(List.of(discountPct));

        app.setForms(List.of(
                app.getForms().get(0),
                customer
        ));

        return app;
    }

    @Test
    void shouldDetectUnknownFieldReference() {
        App app = createApp("Quantity * UnknownPrice");

        List<UnknownFieldReference> issues =
                analyzer.findUnknownReferences(app);

        assertEquals(1, issues.size());
        assertEquals("Order.Total", issues.get(0).getSourceField());
        assertEquals(
                "UnknownPrice",
                issues.get(0).getReferencedField()
        );
    }

    @Test
    void shouldAcceptReferencesToExistingFields() {
        App app = createApp("Quantity * Price");

        List<UnknownFieldReference> issues =
                analyzer.findUnknownReferences(app);

        assertTrue(issues.isEmpty());
    }

    @Test
    void shouldDetectMultipleUnknownReferences() {
        App app = createApp(
                "Quantity * MissingPrice + MissingTax"
        );

        List<UnknownFieldReference> issues =
                analyzer.findUnknownReferences(app);

        assertEquals(2, issues.size());

        assertEquals(
                List.of("MissingPrice", "MissingTax"),
                issues.stream()
                        .map(UnknownFieldReference::getReferencedField)
                        .toList()
        );
    }

    @Test
    void shouldAcceptValidQualifiedReference() {
        App app = createAppWithCustomer(
                "Customer.DiscountPct * Quantity"
        );

        List<UnknownFieldReference> issues =
                analyzer.findUnknownReferences(app);

        assertTrue(issues.isEmpty());
    }

    @Test
    void shouldDetectUnknownFormInQualifiedReference() {
        App app = createApp(
                "UnknownForm.DiscountPct * Quantity"
        );

        List<UnknownFieldReference> issues =
                analyzer.findUnknownReferences(app);

        assertEquals(1, issues.size());
        assertEquals(
                "UnknownForm.DiscountPct",
                issues.get(0).getReferencedField()
        );
    }

    @Test
    void shouldDetectUnknownFieldInExistingForm() {
        App app = createAppWithCustomer(
                "Customer.UnknownField * Quantity"
        );

        List<UnknownFieldReference> issues =
                analyzer.findUnknownReferences(app);

        assertEquals(1, issues.size());
        assertEquals(
                "Customer.UnknownField",
                issues.get(0).getReferencedField()
        );
    }
}
