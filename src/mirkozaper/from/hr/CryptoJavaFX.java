/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mirkozaper.from.hr;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author mirko
 */
public class CryptoJavaFX extends Application {
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root =FXMLLoader.load(getClass().getResource("view/Home.fxml"));
        
        Scene scene = new Scene(root);
        
        primaryStage.setTitle("Crypto - Java FX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
