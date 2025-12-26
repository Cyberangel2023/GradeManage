module com.example.grademanage {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.sql;
    requires static lombok;
    requires java.desktop;

    opens com.example.grademanage to javafx.fxml;
    exports com.example.grademanage;
    exports com.example.grademanage.Factory;
    opens com.example.grademanage.Factory to javafx.fxml;
    exports com.example.grademanage.Factory.Impl;
    opens com.example.grademanage.Factory.Impl to javafx.fxml;
    exports com.example.grademanage.Controller;
    opens com.example.grademanage.Controller to javafx.fxml;
    exports com.example.grademanage.Entity;
}