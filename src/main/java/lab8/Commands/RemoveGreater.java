package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.PriorityQueue;

/**
 * Класс команды remove_greater
 *
 * @author Остряков Егор, P3112
 */
public class RemoveGreater extends AbstractCommand {
    public RemoveGreater() {
        name = "remove greater";
        help = "удаляет из коллекции все элементы, превышающие заданный";
        needObjectToExecute = true;
        args = "";
    }

    /**
     * Удаляет из коллекции элементы, здоровье которых больше указанного
     *
     * @param priorityQueue   коллекция, из которой удаляются элементы
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.getLock().lock();
        try {
            if (priorityQueue.size() > 0) {
                PriorityQueue<SpaceMarine> priorityQueueWithComp = new PriorityQueue<>(CommandsManager.GetIdComparator());
                priorityQueueWithComp.addAll(priorityQueue);
                priorityQueueWithComp.add(spaceMarine);
                if (priorityQueue.removeIf(spaceMarine -> {
                    if (spaceMarine.getHealth() > priorityQueueWithComp.stream().max((o1, o2) -> (int) (o1.getId() - o2.getId())).get().getHealth())
                        return (dataBaseManager.removeFromDataBase(spaceMarine));
                    return false;
                })) {
                    commandsManager.printToClient("suc_removeGreater");
                    logger.info("Обновляем колекцию в памяти, так как таблица в БД была изменена");
                    dataBaseManager.updateCollectionFromDataBase(priorityQueue);
                    commandsManager.sendCollectionToClient(priorityQueue);
                } else
                    commandsManager.printToClient("err_removeGreater");
            } else commandsManager.printToClient("err_empty");
            SpaceMarine.idSetter--;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            commandsManager.getLock().unlock();
        }
    }

    @Override
    public void setArgs(String args) {
        this.args = args;
    }
}
