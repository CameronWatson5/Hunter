module com.example.hunter {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.prefs;
    requires eu.hansolo.fx.countries;
    requires eu.hansolo.fx.heatmap;
    requires eu.hansolo.toolboxfx;
    requires eu.hansolo.toolbox;

    exports com.example.hunter;
    exports com.example.hunter.enemies;
    exports com.example.hunter.projectiles;

    opens com.example.hunter to javafx.fxml;
    opens com.example.hunter.enemies to javafx.fxml;
    opens com.example.hunter.projectiles to javafx.fxml;
}