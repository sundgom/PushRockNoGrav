package pushrock.fxui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PushRockApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("PushRock");
        Parent parent = FXMLLoader.load(getClass().getResource("PushRock.fxml"));
        stage.setScene(new Scene(parent));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
