                                                                                                                                                                                                    /*
############################################
||                                        ||
||Client de messagerie graphique (JavaFX) ||
||         Auteur : devblocks{42}         ||
||                                        ||
############################################
                                                                                                                                                                                                    */
package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application //Hérite des attributs et méthodes de la classe Application (JavaFX)
{
    private static Scene scene;
    public static FXMLLoader fxmlLoader = new FXMLLoader();
    private static Stage currentStage;
    private static final String BASE_TITLE = "JavaChatClient-1.0";

    @Override public void start(Stage stage) throws IOException 
    {
        scene = new Scene(loadFXML("/fxml/MainWindow"), 1024, 768);
        stage.setScene(scene);
        stage.setTitle(BASE_TITLE + " - Connexion");
        currentStage = stage;
        stage.show();
    }
    public static void setRoot(String fxml) throws IOException 
    {
        scene.setRoot(loadFXML(fxml));
    }
    private static Parent loadFXML(String fxml) throws IOException 
    {
        fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    public static Stage getCurrentStage()
    {
        return currentStage;
    }
    public static String getBaseTitle()
    {
        return BASE_TITLE;
    }
    public static void main(String[] args) //Point d'entrée
    {
        launch();
    }
}