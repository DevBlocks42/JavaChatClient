package controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ManualWindowController extends MainWindowController 
{
    @FXML private Label manualLabel;
    
    public void initialize() 
    {
        String formattedLabel = manualLabel.getText().replaceAll("\\\\n", System.lineSeparator());
        manualLabel.setText(formattedLabel); 
    }
    @Override @FXML public void onMainMenuValidation(ActionEvent event) throws IOException 
    {
        super.onMainMenuValidation(event);
    }
}