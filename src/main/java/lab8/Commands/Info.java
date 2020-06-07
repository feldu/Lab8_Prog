package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;
import lab8.Server;

import java.util.PriorityQueue;

/**
 * Класс команды info
 *
 * @author Остряков Егор, P3112
 */
public class Info extends AbstractCommand {
    public Info() {
        name = "info";
        help = "выводит в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)";
    }

    /**
     * Выводит инормацию о коллекции
     *
     * @param priorityQueue   коллекция, с которой работает пользователь
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        String answer = "Collection type: " + priorityQueue.getClass().getName() + "\n" +
                "Created: " + Server.GetCreationDate() + "\n" +
                "Collection size: " + priorityQueue.size();

        commandsManager.printToClient("suc_info" + "_" + answer);
    }
}
