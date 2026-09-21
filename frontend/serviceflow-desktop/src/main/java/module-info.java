module com.serviceflow {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.serviceflow.controller to javafx.fxml;
    exports com.serviceflow;
}
