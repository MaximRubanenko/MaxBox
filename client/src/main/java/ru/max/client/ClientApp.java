package ru.max.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientApp extends Application {

    private FXMLLoader loader;
    private Controller controller;
    private static Stage pStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        pStage = primaryStage;
        loader = new FXMLLoader(getClass().getResource("/mainForm.fxml"));
        controller = loader.getController();
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("MxBox");
        primaryStage.show();
    }

    public static Stage getStage() {
        return pStage;
    }

    @Override
    public void stop() {
        try{
            controller = loader.getController();
            controller.getFileWorker().stop();
            Controller.getConnection().close();
        }catch (NullPointerException ignored){}
    }
    public static void main(String[] args) {
        launch();
    }
}
