package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.PriorityQueue;

/**
 * Класс команды remove_by_id
 *
 * @author Остряков Егор, P3112
 */
public class RemoveById extends AbstractCommand {
    public RemoveById() {
        name = "remove by id";
        help = "удаляет элемент из коллекции по его id";
    }

    /**
     * Удаляет элемент по id
     *
     * @param priorityQueue   коллекция, из которой удаляется элемент
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.getLock().lock();
        try {
            long id = Long.parseLong(args);
            if (priorityQueue.removeIf(spaceMarine -> {
                if (spaceMarine.getId() == id) {
                    return (dataBaseManager.removeFromDataBase(spaceMarine));
                }
                return false;
            })) {
                commandsManager.printToClient("suc_remove" + "_" + args);
                logger.info("Обновляем колекцию в памяти, так как таблица в БД была изменена");
                dataBaseManager.updateCollectionFromDataBase(priorityQueue);
                commandsManager.sendCollectionToClient(priorityQueue);
            } else
                commandsManager.printToClient("err_remove");
        } catch (Exception e) {
            commandsManager.printToClient("err_type");
        } finally {
            commandsManager.getLock().unlock();
        }
    }
}
