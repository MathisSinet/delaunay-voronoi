module com.example.poc {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.example.poc to javafx.fxml;
    exports com.example.poc;
}