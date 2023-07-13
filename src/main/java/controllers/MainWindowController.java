package controllers;

import client.App;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

public class MainWindowController 
{
    @FXML private TextField fieldHostname;
    @FXML private TextField fieldLogin;
    @FXML private TextField fieldPortNumber;
    
    @FXML public void onMainMenuValidation(ActionEvent event) throws IOException //Gestion du menu principal (barre du haut).
    {
        MenuItem selectedItem = (MenuItem)event.getSource(); //On récupère le sous-menu actuellement séléctionné par l'utilisateur, puis on charge la fenêtre fxml correspondante.
        switch(selectedItem.getId())
        {
            case "menuServerConnect":
                App.setRoot("/fxml/MainWindow");
                App.getCurrentStage().setTitle(App.getBaseTitle() + " - Connexion");
                break;
            case "menuServerFavorites":
                System.out.println("FAVORIS");
                App.setRoot("/fxml/FavoritesWindow");
                App.getCurrentStage().setTitle(App.getBaseTitle() + " - Favoris");
                break;
            case "menuHelpAbout":
                System.out.println("A propos");
                App.setRoot("/fxml/AboutWindow");
                App.getCurrentStage().setTitle(App.getBaseTitle() + " - À propos");
                break;
            case "menuHelpManual":
                System.out.println("Manuel");
                App.setRoot("/fxml/ManualWindow");
                App.getCurrentStage().setTitle(App.getBaseTitle() + " - Manuel d'utilisation");
                break;
            default: 
                break;
        }
    }
    @FXML public void onConnectButtonClicked(ActionEvent event) throws IOException 
    {
        Alert alert = new Alert(AlertType.ERROR); //On prépare une alert (popup) en cas d'erreur de saisie.
        alert.setTitle("Erreur");
        alert.setHeaderText("Erreur de saisie.");
        if(!fieldHostname.getText().isBlank() && !fieldLogin.getText().isBlank() && !fieldPortNumber.getText().isBlank()) //On vérifie que tous les champs du formulaire ont été saisis.
        {
            int port;
            try
            {
                String hostName = fieldHostname.getText();
                String login = fieldLogin.getText();
                port = Integer.parseInt(fieldPortNumber.getText()); //On converti la chaîne de caractères correspondant au port du serveur en nombre entier.
                App.setRoot("/fxml/ClientWindow");
                ClientWindowController clientWindowController = App.fxmlLoader.<ClientWindowController>getController();
                clientWindowController.init(hostName, port, login); //On transmet les informations de connexion réseau au controlleur de la fenêtre de messagerie.
            }
            catch(NumberFormatException ex) //Integer.parseInt a retourné une exception car il n'a pas réussit a trouver un nombre entier dans la chaîne saisie.
            {
                alert.setContentText("Le numéro de port saisit est invalide, un numéro de port est uniquement constitué de chiffres. Pour plus d'informations, consultez le manuel d'utilisation du programme.");
                alert.showAndWait();
            }           
        }
        else
        {
            alert.setContentText("Vous devez remplir tous les champs du formulaire afin de vous connecter à un serveur. Pour plus d'informations, consultez le manuel d'utilisation du programme.");
            alert.showAndWait();
        }
    }
}