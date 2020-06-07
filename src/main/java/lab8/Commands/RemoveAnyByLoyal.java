package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Класс команды remove_any_by_loyal
 *
 * @author Остряков Егор, P3112
 */
public class RemoveAnyByLoyal extends AbstractCommand {
    public RemoveAnyByLoyal() {
        name = "remove by loyal";
        help = "!!! удаляет из коллекции один элемент, ПРИНАДЛЕЖАЩИЙ ВАМ и значение поля loyal которого эквивалентно заданному";
    }

    /**
     * Удаляет из коллекции один элемент с указанным значением лояльности
     *
     * @param priorityQueue   коллекция, из которой удаляется элемент
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.getLock().lock();
        try {
            if (priorityQueue.size() > 0) {
                Boolean loyal;
                if (args.equalsIgnoreCase("true")) loyal = true;
                else if (args.equalsIgnoreCase("false")) loyal = false;
                else if (args.equals("null")) loyal = null;
                else {
                    commandsManager.printToClient("err_type");
                    return;
                }
                AtomicBoolean breakme = new AtomicBoolean(false);
                Boolean finalLoyal = loyal;
                if (!priorityQueue.removeIf(spaceMarine -> {
                    if (spaceMarine.getLoyal() == finalLoyal && !breakme.get()) {
                        if (dataBaseManager.removeFromDataBase(spaceMarine)) {
                            breakme.set(true);
                            commandsManager.printToClient("suc_remove" + "_" + spaceMarine.getId());
                            logger.info("Обновляем колекцию в памяти, так как таблица в БД была изменена");
                            dataBaseManager.updateCollectionFromDataBase(priorityQueue);
                            commandsManager.sendCollectionToClient(priorityQueue);
                            return true;
                        }
                    }
                    return false;
                })) {
                    commandsManager.printToClient("err_remove");
                }
            } else commandsManager.printToClient("err_empty");
        } catch (Exception e) {
            commandsManager.printToClient("err_type");
        } finally {
            commandsManager.getLock().unlock();
        }
    }
}
