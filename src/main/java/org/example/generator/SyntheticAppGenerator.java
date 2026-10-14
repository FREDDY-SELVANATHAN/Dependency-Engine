
package org.example.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.App;
import org.example.model.Field;
import org.example.model.Form;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SyntheticAppGenerator {

    public App generate(int formCount, int fieldsPerForm) {

        App app = new App();
        app.setName("SyntheticApp");

        List<Form> forms = new ArrayList<>();

        for (int i = 1; i <= formCount; i++) {

            Form form = new Form();
            form.setName("GeneratedForm" + i);

            List<Field> fields = new ArrayList<>();

            for (int j = 1; j <= fieldsPerForm; j++) {

                Field field = new Field();
                field.setName("Field" + j);
                field.setType("text");

                // Generate formulas for selected fields.
                // Field3 depends on Field1 and Field2.
                if (j == 3 && fieldsPerForm >= 3) {
                    field.setFormula("Field1 + Field2");
                }

                // Field4 depends on Field3.
                if (j == 4 && fieldsPerForm >= 4) {
                    field.setFormula("Field3 * 2");
                }

                // Field5 depends on Field3 and Field4.
                if (j == 5 && fieldsPerForm >= 5) {
                    field.setFormula("Field3 + Field4");
                }

                fields.add(field);
            }

            form.setFields(fields);
            forms.add(form);
        }

        app.setForms(forms);
        return app;
    }

    public void saveAsJson(App app, String filePath) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(filePath), app);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to save generated app: " + e.getMessage(),
                    e
            );
        }
    }
}
