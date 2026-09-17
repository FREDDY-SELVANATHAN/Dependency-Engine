package org.example;
import java.util.List;
public class Form {
    private String name;
    private List<Field>fields;
    private List<Validation>validations;
    public String getName(){
        return name;
    }
    public void setName(){
        this.name = name;
    }
    public List<Field> getFields(){
        return fields;
    }
    public void setFields(List<Field>fields){
        this.fields = fields;
    }
    public List<Validation> getValidations(){
        return validations;
    }
    public void setValidations(List<Validation> validations){
        this.validations = validations;
    }
}
