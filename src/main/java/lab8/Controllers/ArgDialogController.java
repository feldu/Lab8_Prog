package lab8.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс контроллера окна для принятия простых аргументов для команды
 *
 * @author Остряков Егор, P3112
 */
public class ArgDialogController {
    private static final Logger logger = LoggerFactory.getLogger(ArgDialogController.class.getName());
    String commandArg = null;
    @FXML
    private TextField inputArg;
    @FXML
    private Label argName;

    @FXML
    private void OKClick(ActionEvent actionEvent) {
        logger.debug("Кнопка ОК нажата");
        commandArg = inputArg.getText();
        logger.debug("Параметры установлены");
        Stage thisStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        thisStage.close();
    }

    @FXML
    private void CancerClick(ActionEvent actionEvent) {
        logger.debug("Кнопка отмена нажата, выходим");
        Stage thisStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        thisStage.close();
    }

    public Label getArgName() {
        return argName;
    }

    public String getCommandArg() {
        return commandArg;
    }
}
