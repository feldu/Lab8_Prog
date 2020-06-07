package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.PriorityQueue;

/**
 * Класс команды show
 *
 * @author Остряков Егор, P3112
 */
public class Show extends AbstractCommand {
    public Show() {
        name = "show";
        help = "показывает элементы коллекции";
    }

    /**
     * Показывает элементы коллекции
     *
     * @param priorityQueue   коллекция, которую нужно показать
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.getLock().lock();
        try {
            commandsManager.sendCollectionToClient(priorityQueue);
        } finally {
            commandsManager.getLock().unlock();
        }
    }
}
