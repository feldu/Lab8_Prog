package lab8.Controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lab8.Collections.MeleeWeapon;
import lab8.Collections.SpaceMarine;
import lab8.Commands.*;
import lab8.ManagersAndHandlers.ClientMessagesHandler;
import lab8.ManagersAndHandlers.FilterTableWorker;
import lab8.ManagersAndHandlers.VisualisationWorker;
import org.google.jhsheets.filtered.FilteredTableView;
import org.google.jhsheets.filtered.tablecolumn.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Класс контроллера основного окна
 *
 * @author Остряков Егор, P3112
 */
public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class.getName());

    private ClientMessagesHandler clientMessagesHandler;
    private FilterTableWorker filterTableWorker;
    private VisualisationWorker visualisationWorker;
    private ResourceBundle currentBundle;

    @FXML
    private FilteredTableView<SpaceMarine> tableMarines;
    @FXML
    private FilterableLongTableColumn<SpaceMarine, Long> idColumn;
    @FXML
    private FilterableStringTableColumn<SpaceMarine, String> nameColumn;
    @FXML
    private FilterableIntegerTableColumn<SpaceMarine, Integer> coordinatesXColumn;
    @FXML
    private FilterableLongTableColumn<SpaceMarine, Long> coordinatesYColumn;
    @FXML
    private FilterableFloatTableColumn<SpaceMarine, Float> healthColumn;
    @FXML
    private FilterableLongTableColumn<SpaceMarine, Long> heartCountColumn;
    @FXML
    private FilterableBooleanTableColumn<SpaceMarine, Boolean> loyalColumn;
    @FXML
    private FilterableEnumTableColumn<SpaceMarine, MeleeWeapon> meleeWeaponColumn;
    @FXML
    private FilterableStringTableColumn<SpaceMarine, String> chapterNameColumn;
    @FXML
    private FilterableStringTableColumn<SpaceMarine, String> chapterWorldColumn;
    @FXML
    private FilterableStringTableColumn<SpaceMarine, String> userColumn;
    @FXML
    private FilterableDateTableColumn<SpaceMarine, LocalDate> creationDateColumn;

    @FXML
    private Label usernameLabel;
    @FXML
    private Pane pane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            currentBundle = ResourceBundle.getBundle("bundles/bundle", new Locale("ru"));
            clientMessagesHandler = new ClientMessagesHandler(this);
            ClientMessagesHandler updateCollectionClientMessagesHandler = new ClientMessagesHandler(this);
            visualisationWorker = new VisualisationWorker(pane, updateCollectionClientMessagesHandler, this);
            filterTableWorker = new FilterTableWorker(tableMarines, idColumn, nameColumn, coordinatesXColumn, coordinatesYColumn, healthColumn, heartCountColumn, loyalColumn, meleeWeaponColumn, chapterNameColumn, chapterWorldColumn, userColumn, creationDateColumn, updateCollectionClientMessagesHandler, visualisationWorker);
            logger.debug("Подготовка к запуску клиента");
            clientMessagesHandler.connect("localhost", 1488);
            logger.debug("Загружаем Authorization.fxml");
            String fxmlFile = "/fxml/Authorization.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();
            AuthorisationController authorizationController = fxmlLoader.getController();
            logger.debug("Передаём параметр {} в контроллер авторизации {}", clientMessagesHandler, authorizationController);
            authorizationController.setClientMessagesHandler(clientMessagesHandler);
            authorizationController.setMainController(this);
            Stage authorizationStage = new Stage();
            authorizationStage.setTitle("Authorisation");
            authorizationStage.setResizable(false);
            authorizationStage.setScene(new Scene(root));
            authorizationStage.requestFocus();
            authorizationStage.initModality(Modality.WINDOW_MODAL);
            logger.debug("Показываем окно авторизации пользователю");
            authorizationStage.showAndWait();
            logger.debug("Авторизация закрыта");
            if (!clientMessagesHandler.isAuthorized()) {
                logger.error("Пользователь не авторизовался, выходим");
                System.exit(0);
            } else {
                logger.debug("Пользователь {} авторизовался", clientMessagesHandler.getLogin());
                usernameLabel.setText(currentBundle.getString("username") + clientMessagesHandler.getLogin());
                fillTable();
                filterTableWorker.setupFilterTable();
                getDataFromTableWithDoubleClick();
            }
            changeLanguage();
        } catch (IOException e) {
            logger.error("Ошибка ввода-вывода: {}" + e.getMessage());
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("IOException") + e.getMessage());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("UnexpectedException") + e.getMessage());
            logger.error("Непредвиденная ошибка {}", e.getMessage());
        }
    }

    /**
     * Заполняем таблицу значениями клиентской коллекции
     */
    public void fillTable() {
        try {
            logger.debug("Отправляем запрос серверу на обновление клиентской коллекции");
            clientMessagesHandler.sendCommand(new Show());
            receiveAnswer();
            filterTableWorker.getSpaceMarinesData().clear();
            logger.debug("Добавляем все элементы из клиентской коллекции в ObservableList");
            filterTableWorker.getSpaceMarinesData().addAll(clientMessagesHandler.getClientPq());
            logger.debug("Устанавливаем тип и значение которое должно хранится в колонке");
            idColumn.setCellValueFactory(spaceMarinesData -> spaceMarinesData.getValue().getIdProperty().asObject());
            nameColumn.setCellValueFactory(spaceMarinesData -> spaceMarinesData.getValue().getNameProperty());
            coordinatesXColumn.setCellValueFactory(spaceMarinesData -> spaceMarinesData.getValue().getCoordinates().getXProperty().asObject());
            coordinatesYColumn.setCellValueFactory(spaceMarinesData -> spaceMarinesData.getValue().getCoordinates().getYProperty().asObject());
            healthColumn.setCellValueFactory(spaceMarinesData -> spaceMarinesData.getValue().getHealthProperty().asObject());
            heartCountColumn.setCellValueFactory(spaceMarinesData -> spaceMarinesData.getValue().getHeartCountProperty().asObject());
            loyalColumn.setCellValueFactory(new PropertyValueFactory<>("loyal"));
            meleeWeaponColumn.setCellValueFactory(new PropertyValueFactory<>("meleeWeapon"));
            chapterNameColumn.setCellValueFactory(spaceMarinesData -> spaceMarinesData.getValue().getChapter().getNameProperty());
            chapterWorldColumn.setCellValueFactory(spaceMarinesData -> spaceMarinesData.getValue().getChapter().getWorldProperty());
            userColumn.setCellValueFactory(spaceMarinesData -> spaceMarinesData.getValue().getCreatedByUserProperty());
            creationDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
            tableMarines.setItems(filterTableWorker.getSpaceMarinesData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Обработчик события "двойной щелчок мышкой по таблице"
     */
    private void getDataFromTableWithDoubleClick() {
        tableMarines.setRowFactory(tv -> {
            TableRow<SpaceMarine> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    if ((!row.isEmpty())) {
                        SpaceMarine selectedMarine = row.getItem();
                        AbstractCommand update = new Update();
                        update.setArgs(String.valueOf(selectedMarine.getId()));
                        update = getCommandWithObject(update, currentBundle.getString("update"), selectedMarine);
                        logger.debug("Обновляем элемент {}", selectedMarine);
                        if (update != null) sendPreparedCommand(update);
                    } else {
                        logger.debug("Добавляем новый элемент");
                        sendPreparedCommand(getCommandWithObject(new Add(), currentBundle.getString("add"), null));
                    }
                }
            });
            return row;
        });
    }

    /**
     * Собираем полученный от сервера ответ в уведомление
     */
    private void receiveAnswer() {
        try {
            logger.debug("Ожидаем ответ");
            String answer;
            StringBuilder content = new StringBuilder();
            while (true) {
                answer = clientMessagesHandler.receiveAnswer();
                if (answer.contains("I am fucking seriously, it's fucking EOF!!!")) break;
                if (!answer.isEmpty() && !answer.equals("collectionReceived")) {
                    logger.debug("Ответ получен: {}", answer);
                    content.append(answer).append("\n");
                }
            }
            if (!content.toString().isEmpty())
                showAlert(Alert.AlertType.INFORMATION, currentBundle.getString("info"), content.toString());

        } catch (IOException e) {
            logger.error("Ошибка ввода-вывода {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("IOException") + e.getMessage());
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("UnexpectedException") + e.getMessage());
        }
    }

    /**
     * Показываем окно уведомлений пользователю
     *
     * @param alertType тип
     * @param title     заголовок
     * @param content   основной текст
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        logger.debug("Окно сообщений открыто");
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                logger.debug("Окно сообщений закрыто");
            }
        });
    }

    /*
    Далее идут обработчики команд без аргументов
    */
    @FXML
    private void help() {
        sendPreparedCommand(new Help());
    }

    @FXML
    private void info() {
        sendPreparedCommand(new Info());
    }

    @FXML
    private void removeFirstClick() {
        sendPreparedCommand(new RemoveFirst());
    }

    @FXML
    private void clearClick() {
        sendPreparedCommand(new Clear());
    }

    @FXML
    private void maxByHealthClick() {
        sendPreparedCommand(new MaxByHealth());
    }

    @FXML
    private void printUniqueHealthClick() {
        sendPreparedCommand(new PrintUniqueHealth());
    }

    @FXML
    private void exit() {
        logger.debug("Команда выхода обнаружена. Выходим.");
        System.exit(0);
    }

    /**
     * Отправляем подготовленную команду на сервер
     *
     * @param command команда
     */
    public void sendPreparedCommand(AbstractCommand command) {
        try {
            if (command != null) {
                clientMessagesHandler.sendCommand(command);
                logger.debug("Команда отправлена");
                receiveAnswer();
                filterTableWorker.getSpaceMarinesData().clear();
                filterTableWorker.getSpaceMarinesData().addAll(clientMessagesHandler.getClientPq());
            } else {
                logger.error("Команда не может быть отправлена, недостаточно данных");
                showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("InvalidData"));
            }
        } catch (IOException e) {
            logger.error("Ошибка ввода-вывода {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("IOException") + e.getMessage());

        } catch (NullPointerException e) {
            logger.error("Ебаный NPE");
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("UnexpectedException") + e.getMessage());

        }
    }

    /*
        Далее идут обработчики команд с простым аргументом
    */

    @FXML
    private void removeByLoyalClick() {
        sendPreparedCommand(getCommandWithSimpleArg(new RemoveAnyByLoyal(), currentBundle.getString("WriteLoyal")));
    }

    @FXML
    private void removeByIdClick() {
        sendPreparedCommand(getCommandWithSimpleArg(new RemoveById(), currentBundle.getString("WriteId")));
    }

    /**
     * Задаёт команде простой аргумент, полученный в диалоговом окне
     *
     * @param command   команда
     * @param labelText имя поля, которое нужно получить
     * @return команду с аргументом, или null, если аргумент получить не удалось
     */
    public AbstractCommand getCommandWithSimpleArg(AbstractCommand command, String labelText) {
        try {
            logger.debug("Загружаем ArgDialog.fxml");
            String fxmlFile = "/fxml/ArgDialog.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            fxmlLoader.setResources(currentBundle);
            Parent root = fxmlLoader.load();
            ArgDialogController argDialogController = fxmlLoader.getController();
            argDialogController.getArgName().setText(labelText);
            logger.debug("Значение label установлено");
            Stage dialogStage = new Stage();
            dialogStage.setTitle(currentBundle.getString("InputData"));
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));
            dialogStage.requestFocus();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            logger.debug("Показываем окно ввода параметров пользователю");
            dialogStage.showAndWait();
            logger.debug("Окно ввода параметров закрыто");
            String receivedArg = argDialogController.getCommandArg();
            if (receivedArg != null) {
                command.setArgs(receivedArg);
                return command;
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("IOException") + e.getMessage());
            logger.error("Ошибка ввода-вывода {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("UnexpectedException") + e.getMessage());

        }
        return null;
    }

    /*
        Далее идут обработчики команд с аргументом в виде объекта
    */
    @FXML
    private void addClick() {
        sendPreparedCommand(getCommandWithObject(new Add(), currentBundle.getString("add"), null));
    }

    @FXML
    private void addIfMinClick() {
        sendPreparedCommand(getCommandWithObject(new AddIfMin(), currentBundle.getString("addIfMin"), null));
    }

    @FXML
    private void removeGreaterClick() {

        sendPreparedCommand(getCommandWithObject(new RemoveGreater(), currentBundle.getString("removeGreater"), null));
    }

    /**
     * Задаёт команде объект
     *
     * @param command     команда
     * @param titleText   заголовок для окна
     * @param spaceMarine объект
     * @return команду с объектом или null, если объект получить не удалось
     */
    public AbstractCommand getCommandWithObject(AbstractCommand command, String titleText, SpaceMarine spaceMarine) {
        try {
            logger.debug("Загружаем SpaceMarineDialog.fxml");
            String fxmlFile = "/fxml/SpaceMarineDialog.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            fxmlLoader.setResources(currentBundle);
            Parent root = fxmlLoader.load();
            SpaceMarineDialogController spaceMarineDialogController = fxmlLoader.getController();
            spaceMarineDialogController.setCurrentBundle(currentBundle);
            Stage dialogStage = new Stage();
            dialogStage.setTitle(titleText);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));
            dialogStage.requestFocus();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            if (spaceMarine != null) {
                spaceMarineDialogController.getNameField().setText(spaceMarine.getName());
                spaceMarineDialogController.getCoordinateXField().setText(String.valueOf(spaceMarine.getCoordinates().getX()));
                spaceMarineDialogController.getCoordinateYField().setText(String.valueOf(spaceMarine.getCoordinates().getY()));
                spaceMarineDialogController.getHealthField().setText(String.valueOf(spaceMarine.getHealth()));
                spaceMarineDialogController.getHeartCountField().setText(String.valueOf(spaceMarine.getHeartCount()));
                if (spaceMarine.getLoyal() != null)
                    spaceMarineDialogController.getLoyalBox().setValue(spaceMarine.getLoyal());
                spaceMarineDialogController.getMeleeWeaponBox().setValue(spaceMarine.getMeleeWeapon());
                spaceMarineDialogController.getChapterNameField().setText(spaceMarine.getChapter().getName());
                spaceMarineDialogController.getChapterWorldField().setText(spaceMarine.getChapter().getWorld());
            }
            logger.debug("Показываем окно ввода параметров пользователю");
            dialogStage.showAndWait();
            spaceMarine = spaceMarineDialogController.getSpaceMarine();
            if (spaceMarine != null) {
                command.setSpaceMarine(spaceMarine);
                return command;
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("IOException") + e.getMessage());
            logger.error("Ошибка ввода-вывода {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("UnexpectedException") + e.getMessage());

        }
        return null;
    }

    @FXML
    private void updateClick() {
        try {
            AbstractCommand command = getCommandWithSimpleArg(new Update(), currentBundle.getString("WriteId"));
            assert command != null;
            final long id = Long.parseLong(command.getArgs());
            SpaceMarine selectedMarine = null;
            for (SpaceMarine spaceMarine : clientMessagesHandler.getClientPq())
                if (spaceMarine.getId() == id)
                    selectedMarine = spaceMarine;
            if (selectedMarine != null) {
                command = getCommandWithObject(command, currentBundle.getString("update"), selectedMarine);
                if (command != null) sendPreparedCommand(command);
            } else {
                logger.error("Элемент с таким id не найден");
                showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("idNotFound"));

            }
        } catch (NumberFormatException e) {
            logger.error("Неверные данные");
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("IncorrectDataField"));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, currentBundle.getString("Error"), currentBundle.getString("UnexpectedException") + e.getMessage());
            logger.error("Непредвиденная ошибка {}", e.getMessage());
        }
    }

    /**
     * Переход к области визуализации
     */
    @FXML
    private void visualisationSelect() {
        visualisationWorker.setupVisualisation();

    }

    /**
     * Обновление таблицы с данными
     */
    @FXML
    private void updateTableClick() {
        filterTableWorker.setUpdateRQ(true);
    }

    /**
     * Переводим на русский
     */
    @FXML
    private void switchRussian() {
        currentBundle = ResourceBundle.getBundle("bundles/bundle", new Locale("ru"));
        changeLanguage();
    }

    /**
     * Переводим на норвежский
     */
    @FXML
    private void switchNorwegian() {
        currentBundle = ResourceBundle.getBundle("bundles/bundle", new Locale("no"));
        changeLanguage();
    }

    /**
     * Переводим на венгерский
     */
    @FXML
    private void switchHungarian() {
        currentBundle = ResourceBundle.getBundle("bundles/bundle", new Locale("hu"));
        changeLanguage();
    }

    /**
     * Переводим на испанский
     */
    @FXML
    private void switchSpanish() {
        currentBundle = ResourceBundle.getBundle("bundles/bundle", new Locale("es", "GT"));
        changeLanguage();
    }

    @FXML
    private Menu menuFile, menuHelp, selectLanguage;
    @FXML
    private MenuItem exit, help, info;
    @FXML
    private Label updateTableLabel, addCmdLabel, removeCmdLabel, updateCmdLabel, printCmdLabel;
    @FXML
    private Button add, addIfMin, removeFirst, removeByLoyal, removeById, removeGreater, clear, update, maxByHealth, printUniqueHealth;
    @FXML
    private Tab tabTable, tabVisual;

    /**
     * Меняем язык на элементах управления
     */
    private void changeLanguage() {
        visualisationWorker.setCurrentBundle(currentBundle);
        //Меню
        menuFile.setText(currentBundle.getString("file"));
        exit.setText(currentBundle.getString("exit"));
        menuHelp.setText(currentBundle.getString("help"));
        help.setText(currentBundle.getString("commandsInfo"));
        info.setText(currentBundle.getString("collectionInfo"));
        selectLanguage.setText(currentBundle.getString("selectLanguage"));
        //Тулбара
        usernameLabel.setText(currentBundle.getString("username") + clientMessagesHandler.getLogin());
        updateTableLabel.setText(currentBundle.getString("updateTableLabel"));
        //Команды
        addCmdLabel.setText(currentBundle.getString("addCmdLabel"));
        add.setText(currentBundle.getString("add"));
        addIfMin.setText(currentBundle.getString("addIfMin"));
        removeCmdLabel.setText(currentBundle.getString("removeCmdLabel"));
        removeFirst.setText(currentBundle.getString("removeFirst"));
        removeByLoyal.setText(currentBundle.getString("removeByLoyal"));
        removeById.setText(currentBundle.getString("removeById"));
        removeGreater.setText(currentBundle.getString("removeGreater"));
        clear.setText(currentBundle.getString("clear"));
        updateCmdLabel.setText(currentBundle.getString("updateCmdLabel"));
        update.setText(currentBundle.getString("update"));
        printCmdLabel.setText(currentBundle.getString("printCmdLabel"));
        maxByHealth.setText(currentBundle.getString("maxByHealth"));
        printUniqueHealth.setText(currentBundle.getString("printUniqueHealth"));
        //Табы
        tabTable.setText(currentBundle.getString("tabTable"));
        tabVisual.setText(currentBundle.getString("tabVisual"));
    }

    /**
     * Переводит spaceMarine на локаль
     *
     * @param spaceMarine spaceMarine
     * @return строку на нужном языке
     */
    public String getSpaceMarineInLocalLanguage(SpaceMarine spaceMarine) {
        return currentBundle.getString("SpaceMarineCreatedByUser") + spaceMarine.getCreatedByUser() + currentBundle.getString("with") +
                "id = " + spaceMarine.getId() +
                currentBundle.getString("SMname") + spaceMarine.getName() + '\'' +
                currentBundle.getString("coordinateX") + spaceMarine.getCoordinates().getX() + ", y=" + spaceMarine.getCoordinates().getX() +
                currentBundle.getString("creationDate") + spaceMarine.getCreationDate() +
                currentBundle.getString("SMhealth") + spaceMarine.getHealth() +
                currentBundle.getString("SMhCnt") + spaceMarine.getHeartCount() +
                currentBundle.getString("SMloyal") + spaceMarine.getLoyal() +
                currentBundle.getString("SMmw") + spaceMarine.getMeleeWeapon() +
                currentBundle.getString("chName") + spaceMarine.getChapter().getName() + currentBundle.getString("chWorld") + spaceMarine.getChapter().getWorld() +
                "}}\n";
    }

    public void setCurrentBundle(ResourceBundle currentBundle) {
        this.currentBundle = currentBundle;
    }

    public ResourceBundle getCurrentBundle() {
        return currentBundle;
    }
}
