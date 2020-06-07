package lab8.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lab8.Collections.Chapter;
import lab8.Collections.Coordinates;
import lab8.Collections.MeleeWeapon;
import lab8.Collections.SpaceMarine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

/**
 * Класс контроллера окна, получающего объект для команды
 *
 * @author Остряков Егор, P3112
 */
public class SpaceMarineDialogController {
    private static final Logger logger = LoggerFactory.getLogger(SpaceMarineDialogController.class.getName());
    private SpaceMarine spaceMarine = null;
    private ResourceBundle currentBundle;

    public void initialize() {
        ObservableList<Boolean> loyalObservableList = FXCollections.observableArrayList(true, false);
        loyalBox.getItems().addAll(loyalObservableList);
        ObservableList<MeleeWeapon> meleeWeaponObservableList = FXCollections.observableArrayList(MeleeWeapon.CHAIN_SWORD, MeleeWeapon.POWER_SWORD, MeleeWeapon.MANREAPER, MeleeWeapon.LIGHTING_CLAW);
        meleeWeaponBox.getItems().addAll(meleeWeaponObservableList);
    }

    @FXML
    private TextField nameField;
    @FXML
    private TextField coordinateXField;
    @FXML
    private TextField coordinateYField;
    @FXML
    private TextField healthField;
    @FXML
    private TextField heartCountField;
    @FXML
    private ComboBox<Boolean> loyalBox;
    @FXML
    private ComboBox<MeleeWeapon> meleeWeaponBox;
    @FXML
    private TextField chapterNameField;
    @FXML
    private TextField chapterWorldField;

    /**
     * Формирование объекта, по данным, введённым в поля
     * @param actionEvent здесь используется для закрытия окна
     */
    @FXML
    private void OKClick(ActionEvent actionEvent) {
        String name = nameField.getText();
        if (name.trim().isEmpty()) {
            showAlert(currentBundle.getString("name"), currentBundle.getString("IncorrectDataField"));
            return;
        }
        int x;
        long y, heartCount;
        float health;
        try {
            x = Integer.parseInt(coordinateXField.getText());
            if (x > 100 || x < -100) {
                showAlert(currentBundle.getString("coordX"), currentBundle.getString("IncorrectNumber"));
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(currentBundle.getString("coordX"), currentBundle.getString("IncorrectDataField"));
            return;
        }
        try {
            y = Long.parseLong(coordinateYField.getText());
            if (y > 100 || y < -100) {
                showAlert(currentBundle.getString("coordY"), currentBundle.getString("IncorrectNumber"));
                return;
            }
        } catch (Exception e) {
            showAlert(currentBundle.getString("coordY"), currentBundle.getString("IncorrectDataField"));
            return;
        }
        try {
            health = Float.parseFloat(healthField.getText());
            if (health <= 0 || health > 1488) {
                showAlert(currentBundle.getString("health"), currentBundle.getString("IncorrectNumber"));
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(currentBundle.getString("health"), currentBundle.getString("IncorrectDataField"));
            return;
        }
        try {
            heartCount = Long.parseLong(heartCountField.getText());
            if (heartCount <= 0 || heartCount > 3) {
                showAlert(currentBundle.getString("heartCount"), currentBundle.getString("IncorrectNumber"));
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(currentBundle.getString("heartCount"), currentBundle.getString("IncorrectDataField"));
            return;
        }
        Boolean loyal = loyalBox.getValue();
        MeleeWeapon meleeWeapon = meleeWeaponBox.getValue();
        if (meleeWeapon == null) {
            showAlert(currentBundle.getString("meleeWeapon"), currentBundle.getString("IncorrectDataField"));
            return;
        }
        String chapterName = chapterNameField.getText();
        if (chapterName.trim().isEmpty()) {
            showAlert(currentBundle.getString("chapterName"), currentBundle.getString("IncorrectDataField"));
            return;
        }
        String chapterWorld = chapterWorldField.getText();
        if (chapterWorld.trim().isEmpty()) {
            showAlert(currentBundle.getString("chapterWorld"), currentBundle.getString("IncorrectDataField"));
            return;
        }
        try {
            spaceMarine = new SpaceMarine(name, new Coordinates(x, y), health, heartCount, loyal, meleeWeapon, new Chapter(chapterName, chapterWorld));
            Stage thisStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            thisStage.close();
        } catch (Exception e) {
            logger.debug("Не удалось собрать объект. Ошибка {}", e.getMessage());
        }

    }

    /**
     * Показывает уведомление ошибки ввода
     * @param fieldName имя поля с ошибкой
     * @param content содержание ошибки
     */
    private void showAlert(String fieldName, String content) {
        logger.debug("Окно сообщений открыто");
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(currentBundle.getString("Error"));
        alert.setHeaderText(currentBundle.getString("IncorrectField") + "\"" + fieldName + "\"");
        alert.setContentText(content);
        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                logger.debug("Окно сообщений закрыто");
            }
        });
    }

    @FXML
    private void CancelClick(ActionEvent actionEvent) {
        logger.debug("Кнопка отмена нажата, выходим");
        Stage thisStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        thisStage.close();
    }

    public SpaceMarine getSpaceMarine() {
        return spaceMarine;
    }

    public TextField getNameField() {
        return nameField;
    }

    public TextField getCoordinateXField() {
        return coordinateXField;
    }

    public TextField getCoordinateYField() {
        return coordinateYField;
    }

    public TextField getHealthField() {
        return healthField;
    }

    public TextField getHeartCountField() {
        return heartCountField;
    }

    public ComboBox<Boolean> getLoyalBox() {
        return loyalBox;
    }

    public ComboBox<MeleeWeapon> getMeleeWeaponBox() {
        return meleeWeaponBox;
    }

    public TextField getChapterNameField() {
        return chapterNameField;
    }

    public TextField getChapterWorldField() {
        return chapterWorldField;
    }

    public void setCurrentBundle(ResourceBundle currentBundle) {
        this.currentBundle = currentBundle;
    }
}
