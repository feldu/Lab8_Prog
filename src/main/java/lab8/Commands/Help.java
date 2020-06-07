package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.PriorityQueue;

/**
 * Класс команды help
 *
 * @author Остряков Егор, P3112
 */
public class Help extends AbstractCommand {
    public Help() {
        name = "help";
        help = "выводит справку по доступным командам";
    }

    /**
     * Выводит справку по командам
     *
     * @param priorityQueue   коллекция, с которой работает пользователь
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.printToClient("suc_help");
    }
}
