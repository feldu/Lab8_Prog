package lab8.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lab8.ManagersAndHandlers.ClientMessagesHandler;
import lab8.Commands.Login;
import lab8.Commands.Register;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Класс контроллера окна авторизации
 *
 * @author Остряков Егор, P3112
 */
public class AuthorisationController {
    private MainController mainController;
    private static final Logger logger = LoggerFactory.getLogger(AuthorisationController.class.getName());
    private ClientMessagesHandler clientMessagesHandler;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Menu selectLanguage;

    /**
     * Вход
     *
     * @param actionEvent здес используется для получения текущего stag'а и его закрытия
     */
    @FXML
    public void loginButtonClick(ActionEvent actionEvent) {
        try {
            logger.debug("Кнопа Войти нажата, устанавливаем логин и пароль клиенту, отправляем команду на сервер");
            String login = usernameField.getText();
            String password = passwordField.getText();
            clientMessagesHandler.setLogin(login);
            clientMessagesHandler.setPassword(password);
            clientMessagesHandler.sendCommand(new Login());
            String answer = "";
            while (answer.isEmpty()) {
                answer = clientMessagesHandler.receiveAnswer();
            }
            if (answer.equals("suc_login")) {
                logger.debug("Получен ответ: \"Пользователь успешно вошёл в систему.\"");
                showAlert(Alert.AlertType.INFORMATION, "Вход", null, mainController.getCurrentBundle().getString("suc_login"));
                Stage authorizedStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                clientMessagesHandler.setAuthorized(true);
                clientMessagesHandler.setLogin(login);
                authorizedStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, mainController.getCurrentBundle().getString("Error"), null, mainController.getCurrentBundle().getString("err_login"));
                logger.error(answer);
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, mainController.getCurrentBundle().getString("Error"), null, mainController.getCurrentBundle().getString("IOException"));
            logger.error("Ошибка ввода-вывода {}, не удалось отправить данные на сервер", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, mainController.getCurrentBundle().getString("Error"), null, mainController.getCurrentBundle().getString("UnexpectedException") + e.getMessage());
            logger.error("Непредвиденная ошибка {}", e.getMessage());
        }
    }

    /**
     * Регистрация
     */
    @FXML
    private void registerButtonClick() {
        try {
            String login = usernameField.getText();
            String password = passwordField.getText();
            clientMessagesHandler.setLogin(login);
            clientMessagesHandler.setPassword(password);
            boolean lessThen4 = login.trim().split("\\s+")[0].length() < 4;
            boolean withSpaces = login.trim().split("\\s+").length != 1;
            boolean invalidChars = !login.trim().split("\\s+")[0].matches("[a-z0-9]+");
            if (!lessThen4 && !withSpaces && !invalidChars) {
                lessThen4 = password.trim().split("\\s+")[0].length() < 4;
                withSpaces = password.trim().split("\\s+").length != 1;
                invalidChars = !password.trim().split("\\s+")[0].matches("[a-z0-9]+");
                if (!lessThen4 && !withSpaces && !invalidChars) {
                    clientMessagesHandler.sendCommand(new Register());
                    String answer = "";
                    while (answer.isEmpty())
                        answer = clientMessagesHandler.receiveAnswer();
                    if (answer.contains("suc_register")) {
                        showAlert(Alert.AlertType.INFORMATION, mainController.getCurrentBundle().getString("info"), null, mainController.getCurrentBundle().getString("suc_register"));
                    } else {
                        showAlert(Alert.AlertType.ERROR, mainController.getCurrentBundle().getString("Error"), null, mainController.getCurrentBundle().getString("err_register"));
                        logger.error(answer);
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, mainController.getCurrentBundle().getString("Error"), mainController.getCurrentBundle().getString("incorrectLogPas"),
                            mainController.getCurrentBundle().getString("uncorrectLogPas"));
                    logger.error("Пароль {} некорректен", password);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, mainController.getCurrentBundle().getString("Error"), mainController.getCurrentBundle().getString("incorrectLogPas"),
                        mainController.getCurrentBundle().getString("uncorrectLogPas"));
                logger.error("Логин {} некорректен", login);
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, mainController.getCurrentBundle().getString("Error"), null, mainController.getCurrentBundle().getString("IOException"));
            logger.error("Не удалось отправить данные на сервер");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, mainController.getCurrentBundle().getString("Error"), null, mainController.getCurrentBundle().getString("UnexpectedException") + e.getMessage());
            logger.error("Непредвиденная ошибка {}", e.getMessage());

        }
    }

    /**
     * Показываем окно уведомлений пользователю
     *
     * @param alertType тип
     * @param title     заголовок
     * @param header    крупный текст
     * @param content   основное сообщение
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


    public void setClientMessagesHandler(ClientMessagesHandler clientMessagesHandler) {
        this.clientMessagesHandler = clientMessagesHandler;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void switchRussian() {
        mainController.setCurrentBundle(ResourceBundle.getBundle("bundles/bundle", new Locale("ru")));
        changeLanguage();
    }

    @FXML
    private void switchNorwegian() {
        mainController.setCurrentBundle(ResourceBundle.getBundle("bundles/bundle", new Locale("no")));
        changeLanguage();
    }

    @FXML
    private void switchHungarian() {
        mainController.setCurrentBundle(ResourceBundle.getBundle("bundles/bundle", new Locale("hu")));
        changeLanguage();
    }

    @FXML
    private void switchSpanish() {
        mainController.setCurrentBundle(ResourceBundle.getBundle("bundles/bundle", new Locale("es", "GT")));
        changeLanguage();
    }

    @FXML
    private Button loginButton, registerButton;
    @FXML
    private Label userName, password;

    /**
     * Меняем язык
     */
    private void changeLanguage() {
        selectLanguage.setText(mainController.getCurrentBundle().getString("selectLanguage"));
        loginButton.setText(mainController.getCurrentBundle().getString("login"));
        registerButton.setText(mainController.getCurrentBundle().getString("register"));
        userName.setText(mainController.getCurrentBundle().getString("username"));
        password.setText(mainController.getCurrentBundle().getString("password"));
    }
}
