package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Класс команды remove_first
 *
 * @author Остряков Егор, P3112
 */
public class RemoveFirst extends AbstractCommand {

    public RemoveFirst() {
        name = "remove first";
        help = "удаляет первый элемент из коллекции";
    }

    /**
     * Удаляет первый элемент коллекции
     *
     * @param priorityQueue   коллекция, из которой удаляется элемент
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.getLock().lock();
        try {
            if (dataBaseManager.removeFromDataBase(priorityQueue.element())) {
                commandsManager.printToClient("suc_remove" + "_" + Objects.requireNonNull(priorityQueue.poll()).getId());
                logger.info("Обновляем колекцию в памяти, так как таблица в БД была изменена");
                dataBaseManager.updateCollectionFromDataBase(priorityQueue);
                commandsManager.sendCollectionToClient(priorityQueue);
            } else commandsManager.printToClient("err_remove");
        } catch (Exception e) {
            commandsManager.printToClient("err_empty");
        } finally {
            commandsManager.getLock().unlock();
        }
    }

    @Override
    public void setArgs(String args) {
        this.args = args;
    }
}
