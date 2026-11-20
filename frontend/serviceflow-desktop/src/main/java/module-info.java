module com.serviceflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.serviceflow.controller to javafx.fxml;
    opens com.serviceflow.controller.admin to javafx.fxml;
    opens com.serviceflow.model to javafx.base;

    exports com.serviceflow;
}
