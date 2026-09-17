package org.example;
import java.util.List;
public class Report {
    private String name;
    private String filter;
    private  String sort;
    private List<String> columns;
    public String getName(){
        return name;
    }
    public String getFilter(){
        return filter;
    }
    public String getSort(){
        return sort;
    }
    public List<String> getColumns() {
        return columns;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setFilter(String filter) {
        this.filter = filter;
    }
    public void setSort(String sort) {
        this.sort = sort;
    }
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
}
