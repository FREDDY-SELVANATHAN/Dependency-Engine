package org.example;

public class Main {
    public static void main(String[] args)throws Exception {
        SyntheticAppGenerator generator = new SyntheticAppGenerator();
        App generatedApp = generator.generate(10,100);
        generator.saveAsJson(generatedApp,"generated-app.json");
        System.out.println("Generated synthetic app successfully!!");

        AppLoader loader = new AppLoader();
        App app = loader.load("generated-app.json");
        System.out.println("App name: "+app.getName());
        System.out.println();
        int totalFields = 0;
        for(Form form : app.getForms()){
            totalFields += form.getFields().size();
        }

        System.out.println("Number of forms: "+app.getForms().size());
        System.out.println("Total fields: "+totalFields);
        System.out.println();

        //Forms
        for (Form form:app.getForms()){
            System.out.println("Form: "+form.getName());

            //Fields
            for(Field field: form.getFields()){
                System.out.println("Field: "+field.getName()+"\nType: "+field.getType());
            }

            //Validations
            if(form.getValidations()!=null){
                for(Validation validation: form.getValidations()){
                    System.out.println("Validation: "+validation.getName()+"\nCondition: "+validation.getCondition());
                }
            }

            System.out.println();
        }

        //Reports
        if(app.getReports()!=null){
            for(Report report:app.getReports()){
                System.out.println("Report: "+report.getName()+
                        " | Filter: "+report.getFilter()+
                        " | Sort: "+report.getSort());
                System.out.println("Columns: "+report.getColumns());
            }
        }

        //Workflows
        if(app.getWorkflows()!=null){
            for(Workflow workflow: app.getWorkflows()){
                System.out.println("Workflow: "+workflow.getName()+
                        " | Condition: "+workflow.getCondition()+
                        " | Action: "+workflow.getAction());
            }
        }
    }
}