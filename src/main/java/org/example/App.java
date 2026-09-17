package org.example;
import java.util.List;
public class App {
        private String name;
        private List<Form>forms;
        private List<Report>reports;
        private List<Workflow>workflows;
        public String getName(){
            return name;
        }
        public  void setName(String name){
            this.name = name;
        }
        public List<Form> getForms(){
            return forms;
        }
        public void setForms(List<Form> forms){
            this.forms = forms;
        }

        public List<Report> getReports() {
            return reports;
        }
        public void setReports(List<Report> reports) {
            this.reports = reports;
        }

        public List<Workflow> getWorkflows() {
            return workflows;
        }
        public void setWorkflows(List<Workflow> workflows) {
            this.workflows = workflows;
        }
}
