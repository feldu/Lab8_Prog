package lab8;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class ClientApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class.getName());

    public static void main(String[] args) {
        logger.debug("Клиент запущен");
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.debug("Загружаем Main.fxml");
            String fxmlFile = "/fxml/Main.fxml";
            FXMLLoader fxmlLoader  = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.centerOnScreen();
            primaryStage.setResizable(true);
            primaryStage.setTitle("Client Application");
            primaryStage.setMinWidth(640);
            primaryStage.setMinHeight(480);
            logger.debug("Показываем основное окно");
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Ошибка ввода-вывода {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        logger.debug("Клиент завершил работу");
        System.exit(0);
    }
}
