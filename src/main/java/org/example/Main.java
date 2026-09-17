package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class Main {
    public static void main(String[] args)throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            App app = mapper.readValue(new File("app.json"),App.class);
            System.out.println("App name: "+app.getName());
            System.out.println();
        for (Form forms:app.getForms()){
                System.out.println("Form: "+forms.getName());
                for(Field fields: forms.getFields()){
                    System.out.println("Field: "+fields.getName()+"\nType: "+fields.getType());
                }
                System.out.println();
            }
     }
    }

