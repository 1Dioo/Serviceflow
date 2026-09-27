module com.serviceflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.serviceflow.controller to javafx.fxml;
    exports com.serviceflow;
}
