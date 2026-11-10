module pataverde {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    // Permite a JavaFX acceder a App y lanzar la ventana
    exports cl.duoc.pataverde;
    exports cl.duoc.pataverde.model;

    // Permite la inyección de FXML y la serialización con Gson por reflexión
    opens cl.duoc.pataverde to javafx.fxml;
    opens cl.duoc.pataverde.controller to javafx.fxml;
    opens cl.duoc.pataverde.model to com.google.gson;
}