package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

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
            mapper.writeValue(new File(filePath),app);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to save generated app: "
                            + e.getMessage());
        }
    }
}