package org.example;

public class Field {
    private String name;
    private String type;
    private String formula;
    private String lookup;

    public String getName(){
        return name;
    }
    public String getType(){
        return type;
    }
    public String getFormula(){
        return formula;
    }
    public String getLookup(){
        return lookup;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setType(String type){
        this.type = type;
    }
    public void setFormula(String formula){
        this.formula = formula;
    }
    public void setLookup(String lookup){
        this.lookup = lookup;
    }

}
