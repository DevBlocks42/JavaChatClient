package controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AboutWindowController extends MainWindowController 
{
    @FXML private Label aboutLabel;
    
    public void initialize() //Appellé automatiquement lors du chargement du contrôleur de la fenêtre.
    {
        String formattedLabel = aboutLabel.getText().replaceAll("\\\\n", System.lineSeparator());
        aboutLabel.setText(formattedLabel); //On remplace toutes les occurences de "\n" par une nouvelle ligne.
    }
    @Override @FXML public void onMainMenuValidation(ActionEvent event) throws IOException 
    {
        super.onMainMenuValidation(event);
    }
}