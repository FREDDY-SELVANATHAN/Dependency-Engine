package org.example;
import java.util.List;
public class App {
        private String name;
        private List<Form>forms;
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
}
