
package org.example.analysis;

import org.example.model.App;
import org.example.model.Field;
import org.example.model.Form;
import org.junit.jupiter.api.Test;

import org.example.model.Report;
import org.example.model.Validation;
import org.example.model.Workflow;
import org.example.parser.DependencyAnalyzer;
import org.example.graph.DependencyGraph;

import java.util.List;
import java.util.ArrayList;

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

    @Test
    void shouldDetectUnqualifiedReferenceExistingOnlyInAnotherForm() {
        App app = createAppWithCustomer(
                "DiscountPct * Quantity"
        );

        List<UnknownFieldReference> issues =
                analyzer.findUnknownReferences(app);

        assertEquals(1, issues.size());
        assertEquals(
                "DiscountPct",
                issues.get(0).getReferencedField()
        );
        assertEquals(
                "Order.Total",
                issues.get(0).getSourceField()
        );
    }

    @Test
    void shouldAcceptQualifiedReferenceAcrossForms() {
        App app = createAppWithCustomer(
                "Customer.DiscountPct + Price"
        );

        List<UnknownFieldReference> issues =
                analyzer.findUnknownReferences(app);

        assertTrue(issues.isEmpty());
    }

    @Test
    void shouldDetectMalformedFormula() {
        App app = createApp("Quantity * +");

        List<FormulaValidationError> errors =
                analyzer.findFormulaErrors(app);

        assertEquals(1, errors.size());
        assertEquals(
                "Order.Total",
                errors.get(0).getSourceField()
        );
        assertEquals(
                "Quantity * +",
                errors.get(0).getFormula()
        );
        assertNotNull(errors.get(0).getMessage());
        assertFalse(errors.get(0).getMessage().isBlank());
    }

    @Test
    void shouldAcceptValidFormulaWithoutErrors() {
        App app = createApp("Quantity * Price");

        List<FormulaValidationError> errors =
                analyzer.findFormulaErrors(app);

        assertTrue(errors.isEmpty());
    }

    private App createOrderTrackerApp() {
        Field customerName = createField("Name", "text", null, null, null);
        Field tier = createField("Tier", "picklist", null, null, null);
        Field discountPct = createField("DiscountPct", "number", null, null, null);

        Form customer = new Form();
        customer.setName("Customer");
        customer.setFields(List.of(customerName, tier, discountPct));

        Field lineTotal = createField(
                "LineTotal", "currency", "Qty * Rate", null, null);

        Form orderItem = new Form();
        orderItem.setName("OrderItem");
        orderItem.setFields(List.of(
                createField("Product", "text", null, null, null),
                createField("Qty", "number", null, null, null),
                createField("Rate", "currency", null, null, null),
                lineTotal
        ));

        Form order = new Form();
        order.setName("Order");
        order.setFields(new ArrayList<>(List.of(
                createField("OrderNo", "text", null, null, null),
                createField("Customer", "lookup", null, "Customer", null),
                createField("Items", "subform", null, null, "OrderItem"),
                createField("SubTotal", "currency", "Sum(Items.LineTotal)", null, null),
                createField("Discount", "currency",
                        "SubTotal * Customer.DiscountPct / 100", null, null),
                createField("Total", "currency", "SubTotal - Discount", null, null),
                createField("Label", "text",
                        "Concat(OrderNo, \" - \", Customer.Name)", null, null)
        )));

        App app = new App();
        app.setForms(List.of(customer, orderItem, order));
        return app;
    }

    private Field createField(
            String name,
            String type,
            String formula,
            String lookup,
            String subform) {

        Field field = new Field();
        field.setName(name);
        field.setType(type);
        field.setFormula(formula);
        field.setLookup(lookup);
        field.setSubform(subform);
        return field;
    }

    @Test
    void shouldResolveLookupPathToCustomerName() {
        App app = createOrderTrackerApp();
        setOrderFormula(app, "Customer.Name");

        assertTrue(analyzer.findUnknownReferences(app).isEmpty());
    }

    @Test
    void shouldResolveLookupPathToDiscountPct() {
        App app = createOrderTrackerApp();
        setOrderFormula(app, "Customer.DiscountPct");

        assertTrue(analyzer.findUnknownReferences(app).isEmpty());
    }

    @Test
    void shouldResolveSubformPathToLineTotal() {
        App app = createOrderTrackerApp();
        setOrderFormula(app, "Items.LineTotal");

        assertTrue(analyzer.findUnknownReferences(app).isEmpty());
    }

    @Test
    void shouldDetectUnknownFieldInLookupPath() {
        App app = createOrderTrackerApp();
        setOrderFormula(app, "Customer.UnknownField");

        assertTrue(analyzer.findUnknownReferences(app).stream()
                .anyMatch(issue ->
                        issue.getReferencedField().equals("Customer.UnknownField")));
    }

    @Test
    void shouldDetectUnknownFieldInSubformPath() {
        App app = createOrderTrackerApp();
        setOrderFormula(app, "Items.UnknownField");

        assertTrue(analyzer.findUnknownReferences(app).stream()
                .anyMatch(issue ->
                        issue.getReferencedField().equals("Items.UnknownField")));
    }

    @Test
    void shouldRejectPathContinuingBeyondOrdinaryField() {
        App app = createOrderTrackerApp();
        setOrderFormula(app, "Customer.Name.First");

        assertTrue(analyzer.findUnknownReferences(app).stream()
                .anyMatch(issue ->
                        issue.getReferencedField().equals("Customer.Name.First")));
    }

    @Test
    void shouldDetectUnknownFormInPath() {
        App app = createOrderTrackerApp();
        setOrderFormula(app, "UnknownForm.Name");

        assertTrue(analyzer.findUnknownReferences(app).stream()
                .anyMatch(issue ->
                        issue.getReferencedField().equals("UnknownForm.Name")));
    }

    private void setOrderFormula(App app, String formula) {
        Form order = app.getForms().stream()
                .filter(form -> "Order".equals(form.getName()))
                .findFirst()
                .orElseThrow();

        Field total = order.getFields().stream()
                .filter(field -> "Total".equals(field.getName()))
                .findFirst()
                .orElseThrow();

        total.setFormula(formula);
    }

    @Test
    void shouldExtractReportFilterDependencies() {
        App app = createApp("Quantity * Price");

        Report report = new Report();
        report.setName("BigOrders");
        report.setFilter("Total > 10000");
        app.setReports(List.of(report));

        List<ComponentDependency> results =
                analyzer.analyzeComponents(app);

        assertTrue(results.stream().anyMatch(result ->
                result.getComponentType().equals("REPORT")
                        && result.getComponentName().equals("BigOrders.filter")
                        && result.getDependencies().contains("Total")));
    }

    @Test
    void shouldExtractReportColumnDependencies() {
        App app = createApp("Quantity * Price");

        Report report = new Report();
        report.setName("BigOrders");
        report.setColumns(List.of("Total", "Customer.Name"));
        app.setReports(List.of(report));

        List<ComponentDependency> results =
                analyzer.analyzeComponents(app);

        assertTrue(results.stream().anyMatch(result ->
                result.getComponentName().equals("BigOrders.column")
                        && result.getExpression().equals("Customer.Name")
                        && result.getDependencies().contains("Customer.Name")));
    }

    @Test
    void shouldExtractValidationDependencies() {
        App app = createApp("Quantity * Price");

        Validation validation = new Validation();
        validation.setName("PositiveTotal");
        validation.setCondition("Total > 0");

        Form order = app.getForms().get(0);
        order.setValidations(List.of(validation));

        List<ComponentDependency> results =
                analyzer.analyzeComponents(app);

        assertTrue(results.stream().anyMatch(result ->
                result.getComponentType().equals("VALIDATION")
                        && result.getComponentName()
                        .equals("Order.PositiveTotal.condition")
                        && result.getDependencies().contains("Total")));
    }

    @Test
    void shouldExtractWorkflowConditionAndActionDependencies() {
        App app = createApp("Quantity * Price");

        Workflow workflow = new Workflow();
        workflow.setName("NotifyBigOrder");
        workflow.setCondition("Total > 10000");
        workflow.setAction("notify(Customer.Name)");

        app.setWorkflows(List.of(workflow));

        List<ComponentDependency> results =
                analyzer.analyzeComponents(app);

        assertTrue(results.stream().anyMatch(result ->
                result.getComponentName()
                        .equals("NotifyBigOrder.condition")
                        && result.getDependencies().contains("Total")));

        assertTrue(results.stream().anyMatch(result ->
                result.getComponentName()
                        .equals("NotifyBigOrder.action")
                        && result.getDependencies().contains("Customer.Name")));
    }
    @Test
    void shouldResolveLookupDependency() {
        App app = new App();

        Form customer = new Form();
        customer.setName("Customer");

        Field name = new Field();
        name.setName("Name");
        name.setType("text");
        customer.setFields(List.of(name));

        Form order = new Form();
        order.setName("Order");

        Field customerLookup = new Field();
        customerLookup.setName("Customer");
        customerLookup.setType("lookup");
        customerLookup.setLookup("Customer");

        Field label = new Field();
        label.setName("Label");
        label.setType("formula");
        label.setFormula("Customer.Name");

        order.setFields(List.of(customerLookup, label));
        app.setForms(List.of(customer, order));

        DependencyGraph graph = new DependencyAnalyzer().buildGraph(app);

        assertTrue(
                graph.getDependencies("Order.Label").contains("Customer.Name")
        );
    }

    @Test
    void shouldResolveSubformDependency() {
        App app = new App();

        Form item = new Form();
        item.setName("OrderItem");

        Field lineTotal = new Field();
        lineTotal.setName("LineTotal");
        lineTotal.setType("formula");
        lineTotal.setFormula("Qty * Rate");

        item.setFields(List.of(lineTotal));

        Form order = new Form();
        order.setName("Order");

        Field items = new Field();
        items.setName("Items");
        items.setType("subform");
        items.setSubform("OrderItem");

        Field subtotal = new Field();
        subtotal.setName("SubTotal");
        subtotal.setType("formula");
        subtotal.setFormula("Sum(Items.LineTotal)");

        order.setFields(List.of(items, subtotal));
        app.setForms(List.of(item, order));

        DependencyGraph graph = new DependencyAnalyzer().buildGraph(app);

        assertTrue(
                graph.getDependencies("Order.SubTotal")
                        .contains("OrderItem.LineTotal")
        );
    }
}
