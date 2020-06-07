package lab8.ManagersAndHandlers;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lab8.Collections.SpaceMarine;
import lab8.Commands.AbstractCommand;
import lab8.Commands.RemoveById;
import lab8.Commands.Update;
import lab8.Controllers.MainController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Класс для того, чтобы наблюдать разноцветные овалы в естественной среде
 *
 * @author Остряков Егор, P3112
 */
public class VisualisationWorker {
    private static final Logger logger = LoggerFactory.getLogger(VisualisationWorker.class.getName());
    private ResourceBundle currentBundle;
    private MainController mainController;
    private ClientMessagesHandler updateCollectionClientMessagesHandler;
    private Pane pane;
    private HashMap<String, Color> users = new HashMap<>();
    private HashMap<Circle, SpaceMarine> circleSpaceMarineHashMap = new HashMap<>();

    public VisualisationWorker(Pane pane, ClientMessagesHandler updateCollectionClientMessagesHandler, MainController mainController) {
        this.pane = pane;
        this.updateCollectionClientMessagesHandler = updateCollectionClientMessagesHandler;
        this.mainController = mainController;
        currentBundle = mainController.getCurrentBundle();
    }

    /**
     * Настройка области визуализации
     */
    public void setupVisualisation() {
        logger.debug("Настаиваем визуализацию");
        setChangeSizeListeners();
        circleSpaceMarineHashMap.clear();
        pane.getChildren().clear();
        PriorityQueue<SpaceMarine> spaceMarines = updateCollectionClientMessagesHandler.getClientPq();
        for (SpaceMarine spaceMarine : spaceMarines) {
            setupMarine(spaceMarine);
        }
        logger.debug("Все объекты добавлены");
    }

    /**
     * Добавление объекта в область
     *
     * @param spaceMarine объект
     */
    private void setupMarine(SpaceMarine spaceMarine) {
        new Random();
        if (!users.containsKey(spaceMarine.getCreatedByUser()))
            users.put(spaceMarine.getCreatedByUser(), Color.color(Math.random(), Math.random(), Math.random()));
        float radius = 16 + 10 * spaceMarine.getHealth() / 1488;
        Circle circle = new Circle(radius * (pane.getHeight() + pane.getWidth() * 0.666) / 1488);
        setCoordinates(circle, spaceMarine.getCoordinates().getX(), spaceMarine.getCoordinates().getY());
        circle.setStroke(users.get(spaceMarine.getCreatedByUser()));
        circle.setFill(users.get(spaceMarine.getCreatedByUser()).deriveColor(1, 1, 1, 0.7));
        circle.setOnMousePressed(circleOnMousePressedEventHandler);
        circleSpaceMarineHashMap.put(circle, spaceMarine);
        pane.getChildren().add(circle);
    }

    /**
     * Метод для установления координат
     *
     * @param circle объект
     * @param x      ненормальное x
     * @param y      ненормальное y
     */
    private void setCoordinates(Circle circle, double x, double y) {
        double newX = x * pane.getWidth() / 200 + pane.getWidth() / 2;
        double newY = -y * pane.getHeight() / 200 + pane.getHeight() / 2;
        circle.setLayoutX(newX);
        circle.setLayoutY(newY);
    }

    /**
     * Обработчики СПЕЦИАЛЬНО ДЛЯ @lary9896
     */
    private void setChangeSizeListeners() {
        pane.widthProperty().addListener((ChangeListener<? super Number>) (observable, oldValue, newValue) -> {
            pane.getChildren().clear();
            for (SpaceMarine spaceMarine : updateCollectionClientMessagesHandler.getClientPq()) {
                setupMarine(spaceMarine);
            }
        });
        pane.heightProperty().addListener((ChangeListener<? super Number>) (observable, oldValue, newValue) -> {
            pane.getChildren().clear();
            for (SpaceMarine spaceMarine : updateCollectionClientMessagesHandler.getClientPq()) {
                setupMarine(spaceMarine);
            }
        });
    }

    /**
     * Обработчик для отображения инфы об объекте при щелчке на него
     */
    EventHandler<MouseEvent> circleOnMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent t) {
            if (t.getSource() instanceof Circle) {
                Circle selectedCircle = ((Circle) (t.getSource()));
                SpaceMarine selectedMarine = circleSpaceMarineHashMap.get(selectedCircle);
                logger.debug("Выбрана: {}", selectedMarine);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(currentBundle.getString("info"));
                alert.setHeaderText(currentBundle.getString("uPick"));
                alert.setContentText(mainController.getSpaceMarineInLocalLanguage(selectedMarine));
                ButtonType close = new ButtonType(currentBundle.getString("exit"));
                ButtonType edit = new ButtonType(currentBundle.getString("update"));
                ButtonType delete = new ButtonType(currentBundle.getString("removeById"));
                alert.getButtonTypes().clear();
                alert.getButtonTypes().addAll(delete, edit, close);
                Optional<ButtonType> option = alert.showAndWait();
                if (option.get() == close)
                    alert.close();
                else if (option.get() == edit) {
                    logger.debug("Переходим к редактированию");
                    AbstractCommand update = new Update();
                    update.setArgs(String.valueOf(selectedMarine.getId()));
                    mainController.sendPreparedCommand(mainController.getCommandWithObject(update, currentBundle.getString("update"), selectedMarine));

                } else if (option.get() == delete) {
                    logger.debug("Переходим к удалению");
                    AbstractCommand removeById = new RemoveById();
                    removeById.setArgs(String.valueOf(selectedMarine.getId()));
                    mainController.sendPreparedCommand(removeById);
                }
            }
        }
    };

    public void setCurrentBundle(ResourceBundle currentBundle) {
        this.currentBundle = currentBundle;
    }
}


