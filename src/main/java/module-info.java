module client.javachatclient 
{
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.jsobject;
    opens client to javafx.fxml, javafx.web;
    opens controllers to javafx.fxml, javafx.web;
    exports client;
    exports controllers;
}
