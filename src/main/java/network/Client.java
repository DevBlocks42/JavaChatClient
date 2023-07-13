package network;

import controllers.ClientWindowController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class Client extends Service<Void> 
{
    private String login = null;
    private final ClientWindowController clientWindowController;
    private final PrintWriter output;
    private final BufferedReader input;
    private final Socket socket;
    private boolean logged = false;
    private boolean hasDisconnect = false;
    
    public Client(String hostname, String login, int port, ClientWindowController clientWindowController) throws IOException //Initialisation des composants réseaux nécéssaires à la connexion du client.
    {
        this.login = login;
        this.clientWindowController = clientWindowController;
        this.socket = new Socket(hostname, port);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8")); //On s'assure que le flux d'entrée sera encodée en utf-8 dans le BufferedReader
        this.output = new PrintWriter(this.socket.getOutputStream());
    }
    public synchronized void sendMessage(String message) throws UnsupportedEncodingException 
    {
        this.output.write(new String(message.getBytes(), "UTF-8")); //On forme une chaîne de caractère composée des octets de la chaîne passée en argument, en l'encodant en utf-8 avant de l'envoyer.
        this.output.flush();
    }
    @Override protected Task<Void> createTask() 
    {
        return new Task<Void>()
        {
            @Override protected Void call() throws Exception 
            {
                sendMessage(login + "\n");
                while(socket.isConnected())
                {
                    String in = input.readLine();
                    if(in != null)
                    {
                        if(!logged)//Si l'utilisateur n'est pas encore authentifié.
                        {
                            if(in.contains("CONNECTION_SUCCESSFULL"))
                            {
                                logged = true;
                                Platform.runLater(()-> { clientWindowController.updateContent(in + "<br>"); }); 
                                sendMessage("/clients\n"); //Connexion du client réussie, on demande quels utilisateurs sont actuellement connectés au serveur (pour les afficher).
                            }
                            else 
                            {
                                clientWindowController.updateContent("<p style = 'color:red;'>[Erreur] Impossible de s'authentifier au serveur, veuillez réessayer avec un autre pseudonyme.</p><br>");
                                break;
                            }
                        }
                        else
                        {
                            if(in.contains("SERVER_CLIENT_LIST"))
                            {
                                System.out.println(in);
                                if(!hasDisconnect)
                                {
                                    Platform.runLater(()-> 
                                    {
                                        clientWindowController.updateClients(in.substring(19, in.length() - 1)); //Mise à jour de l'interface graphique pour afficher les clients connectés.
                                    });
                                }
                                else 
                                {
                                    Platform.runLater(()-> 
                                    {
                                        clientWindowController.updateDisconnectedClients(in.substring(19, in.length() - 1));
                                    });
                                    hasDisconnect = false;
                                }
                            }
                            else if(in.contains("CONNECTION_SUCCESSFULL"))
                            {
                                sendMessage("/clients\n");
                                Platform.runLater(()-> 
                                {
                                    clientWindowController.updateContent(in.replace("\n", "<br>")); /////////
                                });
                            }
                            else if(in.contains("SERVER_CLIENT_DISCONNECT"))
                            {
                                sendMessage("/clients\n");
                                hasDisconnect = true;
                            }
                            else 
                            {
                                clientWindowController.updateContent(in.replace("\n", "<br>"));
                            }
                        }
                    }
                } //Sortie de la boucle principale, le client est déconnecté du serveur, on ferme tous les objets liés au réseau :
                logged = false;
                output.close();
                input.close();
                socket.close();
                return null;
            }
        };
    }
}