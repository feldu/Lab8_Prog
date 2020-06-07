package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.PriorityQueue;

/**
 * Класс команды update
 *
 * @author Остряков Егор, P3112
 */
public class Update extends AbstractCommand {
    public Update() {
        name = "update";
        help = "обновляет значение элемента коллекции, id которого равен заданному";
        needObjectToExecute = true;
    }

    /**
     * Обновляет элемент коллекции по id
     *
     * @param priorityQueue   коллекция, элемент которой нужно обновить
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.getLock().lock();
        try {
            spaceMarine.setId(Long.parseLong(getArgs()));
            if (dataBaseManager.updateElementInDataBase(spaceMarine)) {
                commandsManager.printToClient("suc_update" + "_" + spaceMarine.getId());
                logger.info("Обновляем колекцию в памяти, так как таблица в БД была изменена");
                dataBaseManager.updateCollectionFromDataBase(priorityQueue);
                commandsManager.sendCollectionToClient(priorityQueue);
            } else
                commandsManager.printToClient("err_remove");
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            commandsManager.printToClient("err_type");
        } finally {
            commandsManager.getLock().unlock();
        }
    }

    @Override
    public void setArgs(String args) {
        this.args = args;
    }
}
