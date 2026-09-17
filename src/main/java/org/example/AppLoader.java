package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
public class AppLoader {
    private final ObjectMapper mapper;

    public AppLoader(){
        mapper = new ObjectMapper();
    }

    public App load(String filePath) {
        try {
            File file = new File(filePath);
            App app = mapper.readValue(file,App.class);
            validateApp(app);
            return app;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load app"+e.getMessage());
        }
    }

    private void validateApp(App app) {
        if (app.getName() == null || app.getName().isBlank()) {
            throw new RuntimeException("App name is missing");
        }
        if (app.getForms() == null) {
            throw new RuntimeException("Forms are missing");
        }
        for (Form form : app.getForms()) {
            if (form.getName() == null || form.getName().isBlank()) {
                throw new RuntimeException("Form name is missing");
            }
            if (form.getFields() == null) {
                throw new RuntimeException(
                        "Fields are missing in form: " + form.getName()
                );
            }
            for (Field field : form.getFields()) {
                if (field.getName() == null || field.getName().isBlank()) {
                    throw new RuntimeException(
                            "Field name is missing in form: " + form.getName()
                    );
                }
                if (field.getType() == null || field.getType().isBlank()) {
                    throw new RuntimeException(
                            "Field type is missing for: "
                                    + form.getName() + "." + field.getName()
                    );
                }
            }
        }
    }
}
