package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class Main {
    public static void main(String[] args)throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            App app = mapper.readValue(new File("app.json"),App.class);
            System.out.println("App name: "+app.getName());
            System.out.println();
            //Forms
            for (Form form:app.getForms()){
                    System.out.println("Form: "+form.getName());
                    //Fields
                    for(Field field: form.getFields()){
                        System.out.println("Field: "+field.getName()+"\nType: "+field.getType());
                    }
                    //Validations
                    for(Validation validation: form.getValidations()){
                        System.out.println("Validation: "+validation.getName()+"\nCondition: "+validation.getCondition());
                    }
                System.out.println();
                }
            //Reports
            for(Report report:app.getReports()){
                System.out.println("Report: "+report.getName()+
                        " | Filter: "+report.getFilter()+
                        " | Sort: "+report.getSort());
                System.out.println("Columns: "+report.getColumns());
            }
            //Workflows
            for(Workflow workflow: app.getWorkflows()) {
                System.out.println("Workflow: "+workflow.getName()+
                        " | Condition: "+workflow.getCondition()+
                        " | Action: "+workflow.getAction());
            }
        }
    }


