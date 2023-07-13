package controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import network.Client;

public class ClientWindowController extends MainWindowController //Hérite de la méthode de gestion du menu du haut (voir ligne 132)
{
    private String hostname;
    private int port;
    private Client client;
    private String login;
    private WebEngine webEngine;
    private String content = "";
    private boolean contentUpdated = false;
    @FXML private WebView webView;
    @FXML private TextArea inputTextArea;
    @FXML private ListView<String> onlineUsersTable;
    
    public void init(String hostname, int port, String login) throws IOException
    {
        this.hostname = hostname;
        this.port = port;
        this.login = login;
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true); //On autorise du code javascript à être exécuté côté client.
        webEngine.setUserStyleSheetLocation(getClass().getResource("/css/Style.css").toString());
        webEngine.getLoadWorker().stateProperty().addListener
        (
            new ChangeListener<State>() 
            {
                @Override public void changed(ObservableValue ov, State oldState, State newState)
                {
                    if(newState == Worker.State.SUCCEEDED) 
                    {
                        webEngine.executeScript("window.scrollTo(0, document.body.scrollHeight);"); //On exécute du javascript sur le WebEngine afin de s'assurer que le dernier message reçu est visible (on "scroll" jusqu'au dernier message).
                    }
                }
            }
        );
        client = new Client(hostname, login, port, this);
        updateContent("<div id='clientToClient'><p>[*] Tentative de connexion à l'adresse <b>" + this.hostname + "</b> sur le port <b>" + this.port + "</b> avec le pseudo <b>" + this.login + "</b>.</p></div>");
        sendClientSideMessage();
        Service<Void> clientUpdate = client; 
        Service<Void> uiUpdate = new Service<Void>()
        {
            @Override protected Task<Void> createTask() 
            {
                return new Task<Void>()
                {
                    @Override protected Void call() throws Exception 
                    {
                        while(true) //Tant que la fenêtre est active
                        {
                            if(contentUpdated) //Si le contenu du WebEngine a été modifié.
                            {
                                sendClientSideMessage(); //On recharge le WebEngine avec le nouveau contenu.
                            }
                            Thread.sleep(500);
                        }
                    }
                    
                };
            }   
        }; 
        clientUpdate.start(); //On lance le service de gestion réseau du client (entrées/sorties)
        uiUpdate.start(); //On lance le service de gestion de l'interface utilisateur du client (User Interface)
    }
    public TextArea getTextArea()
    {
        return inputTextArea;
    }
    private void sendClientSideMessage() 
    {
        Platform.runLater(()-> 
        { 
            this.webEngine.loadContent(this.content); 
        });
        contentUpdated = false; 
    }
    public void updateContent(String content)
    {
        this.content += content + "<br>";
        contentUpdated = true;
    }
    public void updateClients(String clients)
    {
        String[] list = clients.split(" ");
        for(String l : list)
        {
            if(!onlineUsersTable.getItems().contains(l))
            {
                onlineUsersTable.getItems().add(l);
            }
        }
    }
    public void updateDisconnectedClients(String clients)
    {
        onlineUsersTable.getItems().remove(0, onlineUsersTable.getItems().size() - 1);
        String[] list = clients.split(" ");
        for(String l : list)
        {
            if(!l.equals(login))
            {
                onlineUsersTable.getItems().add(l);
            }
        }
    }
    @Override @FXML public void onMainMenuValidation(ActionEvent event) throws IOException 
    {
        super.onMainMenuValidation(event); //On appelle la fonction qui gère le menu du haut dans le controlleur principal (évite d'avoir à réécrire le même code).
    }
    @FXML public void onMessageButtonClicked(ActionEvent event) throws UnsupportedEncodingException 
    {
        client.sendMessage(inputTextArea.getText().replace("\n", "<br>") + "<br>\n"); 
        inputTextArea.setText("");
    }
    @FXML public void onClientMenuValidation(ActionEvent event) throws UnsupportedEncodingException
    {
        final String selectedUserName = onlineUsersTable.getSelectionModel().getSelectedItem();
        if(!selectedUserName.equals(login))
        {
            TextInputDialog dialog = showInputPopup("Envoyer un message privé à " + login);
            EventHandler sendMessageEvent = new EventHandler<DialogEvent>()
            {
                @Override public void handle(DialogEvent t) 
                {
                    try
                    {
                        Button sendButton = (Button)dialog.getDialogPane().lookupButton(ButtonType.OK);
                        if(sendButton != null)
                        {
                            TextField inputField = dialog.getEditor();
                            client.sendMessage("/mp " + selectedUserName + " " + inputField.getText() + " \n");
                        }
                    }
                    catch(UnsupportedEncodingException e){}
                }

            };
            dialog.setOnCloseRequest(sendMessageEvent);
        }   
    }
    @FXML public void onToolboxButtonClicked(ActionEvent event) 
    {
        String textSelection = inputTextArea.getSelectedText();
        try
        {
            Button button = (Button)event.getSource();
            String newText = "";
            switch(button.getId()) //Code customizé pour éviter à l'utilisateur d'utiliser des balises HTML (l'utilisateur utilise des boutons pour mettre en forme son texte).
            {
                case "quoteButton":
                    newText = "[q]" + textSelection + "[/q]";
                    break;
                case "boldButton":
                    newText = "[b]" + textSelection + "[/b]";
                    break;
                case "italicButton":
                    newText = "[i]" + textSelection + "[/i]";
                    break;
                case "strikeButton":
                    newText = "[s]" + textSelection + "[/s]";
                    break;
                case "underlineButton":
                    newText = "[u]" + textSelection + "[/u]";
                    break;
                default: 
                    break;
            }
            if(!textSelection.isBlank() || !textSelection.isEmpty())
            {
                inputTextArea.setText(inputTextArea.getText().replace(textSelection, newText));
            }
            else 
            {
                inputTextArea.setText(inputTextArea.getText() + newText);
            }
        }
        catch(ClassCastException ex) //Dans le cas on ou n'a pas pu caster l'élément sélectionné en tant que "Button", alors il s'agit forcément du ColorPicker.
        {
            ColorPicker picker = (ColorPicker)event.getSource();
            Color color = picker.getValue();
            inputTextArea.setText(inputTextArea.getText().replace(textSelection, "[p:" + color.toString().replace("0x", "") + "]" + textSelection + "[/p]"));
        }
    }
    public TextInputDialog showInputPopup(String message)
    {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(message);
        dialog.show();
        return dialog;
    }  
}