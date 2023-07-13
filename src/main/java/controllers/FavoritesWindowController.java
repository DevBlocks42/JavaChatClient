package controllers;

import client.App;
import client.Server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

public class FavoritesWindowController extends ClientWindowController 
{
    @FXML private TableView<Server> serverTable;
    @FXML private TableColumn<Server, String> addressColumn;
    @FXML private TableColumn<Server, String> loginColumn;
    @FXML private TableColumn<Server, String> portColumn;
    private final ObservableList<Server> serverList = FXCollections.observableArrayList();
    private List<Server> savedServers;
    
    public void initialize() 
    {
        serverTable.setEditable(true);
        savedServers = loadSavedServers();
        Server server;
        for(int i = 0; i < savedServers.size(); i++)
        {
            server = new Server(savedServers.get(i).hostnameProperty().get(), savedServers.get(i).portProperty().get(), savedServers.get(i).loginProperty().get());
            serverList.add(server);
        }
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("hostname")); 
        portColumn.setCellValueFactory(new PropertyValueFactory<>("port")); 
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login")); 
        serverTable.setItems(serverList);        
    }
    @Override @FXML public void onMainMenuValidation(ActionEvent event) throws IOException 
    {
        super.onMainMenuValidation(event);
    }
    @FXML public void onConnectButtonClicked(ActionEvent event)  
    {
        Server server = serverTable.getSelectionModel().getSelectedItem();
        if(server != null)
        {
            try 
            {
                App.setRoot("/fxml/ClientWindow");
                ClientWindowController clientWindowController = App.fxmlLoader.<ClientWindowController>getController();
                clientWindowController.init(server.hostnameProperty().get(), Integer.parseInt(server.portProperty().get()), server.loginProperty().get()); 
            }
            catch(IOException ex) 
            { 
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur réseau");
                alert.setContentText("Impossible de se connecter au serveur, vérifiez votre connexion internet. Si le problème persiste, il se peut que le serveur soit injoignable.");
                alert.showAndWait();
                try
                {
                    App.setRoot("/fxml/MainWindow");
                }
                catch(IOException ex2) {} 
            }
        }
    }
    @FXML public void onDeleteButtonClicked(ActionEvent event) 
    {
        Server server = serverTable.getSelectionModel().getSelectedItem();
        if(server != null)
        {
            String lineOfInterest = server.hostnameProperty().get() + ";" + server.portProperty().get() + ";" + server.loginProperty().get() + ";";
            try 
            {
                Files.copy(new File("servers.jcs").toPath(), new File("servers_tmp.jcs").toPath());
                FileReader fileReader = new FileReader(new File("servers_tmp.jcs"));
                FileWriter fileWriter = new FileWriter(new File("servers.jcs"));
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                String currentLine;
                while((currentLine = bufferedReader.readLine()) != null)
                {
                    if(currentLine.equals(lineOfInterest))
                    {
                        continue;
                    }
                    bufferedWriter.write(currentLine + System.lineSeparator());
                }
                bufferedWriter.flush();
                bufferedReader.close();
                bufferedWriter.close();
                Files.delete(new File("servers_tmp.jcs").toPath());
                serverTable.getItems().clear();
                this.initialize(); 
            } 
            catch (IOException ex) {}
        }
         
        
    }
    @FXML public void onAddButtonClicked(ActionEvent event) 
    {
        Server server = new Server("", "", "");
        TextInputDialog hostDialog = showInputPopup("Adresse du serveur");
        EventHandler addHostEvent = new EventHandler<DialogEvent>()
        {
            @Override public void handle(DialogEvent t) 
            {
                Button addButton = (Button)hostDialog.getDialogPane().lookupButton(ButtonType.OK);
                if(addButton != null)
                {
                    server.setHostname(hostDialog.getEditor().getText());
                    TextInputDialog portDialog = showInputPopup("Port du serveur");
                    EventHandler addPortEvent = new EventHandler<DialogEvent>()
                    {
                        @Override public void handle(DialogEvent t) 
                        {
                            Button addButton = (Button)portDialog.getDialogPane().lookupButton(ButtonType.OK);
                            if(addButton != null)
                            {
                                server.setPort(portDialog.getEditor().getText());
                                TextInputDialog loginDialog = showInputPopup("Pseudonyme par défaut");
                                EventHandler addLoginEvent = new EventHandler<DialogEvent>()
                                {
                                    @Override public void handle(DialogEvent t) 
                                    {
                                        Button addButton = (Button)loginDialog.getDialogPane().lookupButton(ButtonType.OK);
                                        if(addButton != null)
                                        {
                                            server.setLogin(loginDialog.getEditor().getText());
                                            try 
                                            {
                                                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("servers.jcs", true));
                                                PrintWriter printWriter = new PrintWriter(bufferedWriter);
                                                printWriter.println(server.hostnameProperty().get() + ";" + server.portProperty().get() + ";" + server.loginProperty().get() + ";");
                                                bufferedWriter.close();
                                                serverTable.getItems().clear();
                                                initialize(); 
                                            } 
                                            catch (IOException ex) {}
                                        }              
                                    }
                                };
                                loginDialog.setOnCloseRequest(addLoginEvent);
                            }              
                        }
                    };
                    portDialog.setOnCloseRequest(addPortEvent);
                }              
            }
        };
        hostDialog.setOnCloseRequest(addHostEvent);    
    }
    private List<Server> loadSavedServers()
    {
        List<Server> servers = new ArrayList<Server>();
        try 
        {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("servers.jcs"));
            String currentLine = null;
            int cursor = 0;
            String hostname = null;
            String port = null;
            String login = null;
            try 
            {
                while((currentLine = bufferedReader.readLine()) != null)
                {
                    String[] elements = currentLine.split(";");
                    for(int i = 0; i < elements.length; i++)
                    {
                        Server server = new Server("", "", "");
                        switch(cursor)
                        {
                            case 0:
                                hostname = elements[cursor];
                                cursor++;
                                break;
                            case 1:
                                port = elements[cursor];
                                cursor++;
                                break;
                            case 2:
                                login = elements[cursor];
                                servers.add(new Server(hostname, port, login));
                                cursor = 0;
                                break;
                        }
                        
                    }
                }
                bufferedReader.close();
            } 
            catch (IOException ex) {}
        } 
        catch (FileNotFoundException ex) {}
        return servers;
    }
}