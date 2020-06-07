package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.PriorityQueue;

/**
 * Класс команды clean
 *
 * @author Остряков Егор, P3112
 */
public class Clear extends AbstractCommand {
    public Clear() {
        name = "clear";
        help = "!!! удаляет все элементы коллекции, принадлежащие Вам";
    }

    /**
     * Удаляет все элементы коллекции
     *
     * @param priorityQueue   коллекция, которую нужно очистить
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.getLock().lock();
        try {
            priorityQueue.parallelStream().forEachOrdered(dataBaseManager::removeFromDataBase);
            commandsManager.printToClient("suc_clear");
            logger.info("Обновляем колекцию в памяти, так как таблица в БД была изменена");
            dataBaseManager.updateCollectionFromDataBase(priorityQueue);
            commandsManager.sendCollectionToClient(priorityQueue);
        } finally {
            commandsManager.getLock().unlock();
        }
    }

}
