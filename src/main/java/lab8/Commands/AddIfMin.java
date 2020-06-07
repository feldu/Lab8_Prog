package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.PriorityQueue;

/**
 * Класс команды add_if_min
 *
 * @author Остряков Егор, P3112
 */
public class AddIfMin extends AbstractCommand {
    public AddIfMin() {
        name = "add if min";
        help = "добавляет новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции";
        needObjectToExecute = true;
        args = "";
    }

    /**
     * Добавляет новый элемент в коллекцию, если значение его здоровья минимально
     *
     * @param priorityQueue   коллекция, в которую нужно добавить элемент
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.getLock().lock();
        try {
            PriorityQueue<SpaceMarine> priorityQueueWithMin = new PriorityQueue<>(CommandsManager.GetIdComparator());
            priorityQueueWithMin.addAll(priorityQueue);
            priorityQueueWithMin.add(spaceMarine);
            SpaceMarine minMarine = priorityQueueWithMin.stream().min(CommandsManager.GetIdComparator()).get();
            if (priorityQueue.peek().getHealth() <= minMarine.getHealth()) {
                logger.warn("Элемент не добавлен, так как не является минимальным");
                commandsManager.printToClient("err_addIfMin");
            } else if (dataBaseManager.addToDataBase(minMarine)) {
                commandsManager.printToClient("suc_add");
                logger.info("Элемент добавлен в коллекцию");
                logger.info("Обновляем колекцию в памяти, так как таблица в БД была изменена");
                dataBaseManager.updateCollectionFromDataBase(priorityQueue);
                commandsManager.sendCollectionToClient(priorityQueue);
            }
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            commandsManager.printToClient("err_type");
            logger.error("Неверный тип аргумента: {}", e.getMessage());
        } finally {
            commandsManager.getLock().unlock();
        }
    }

    @Override
    public void setArgs(String args) {
        this.args = args;
    }
}