module com.example.grademanage {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.grademanage to javafx.fxml;
    exports com.example.grademanage;
}