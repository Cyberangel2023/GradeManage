module com.example.grademanage {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.sql;
    requires static lombok;
    requires com.example.grademanage;


    opens com.example.grademanage to javafx.fxml;
    exports com.example.grademanage;
}