package lab8.Commands;

import lab8.Collections.*;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.PriorityQueue;

/**
 * Класс команды add
 *
 * @author Остряков Егор, P3112
 */
public class Add extends AbstractCommand {
    public Add() {
        name = "add";
        help = "добавляет новый элемент в коллекцию";
        needObjectToExecute = true;
        args = "";
    }

    /**
     * Добавляет новый элемент в коллекцию
     *
     * @param priorityQueue   коллекция, в которую нужно добавить элемент
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.getLock().lock();
        try {
            if (dataBaseManager.addToDataBase(spaceMarine)) {
                commandsManager.printToClient("suc_add");
                logger.info("Элемент добавлен в коллекцию");
                logger.info("Обновляем колекцию в памяти, так как таблица в БД была изменена");
                dataBaseManager.updateCollectionFromDataBase(priorityQueue);
                commandsManager.sendCollectionToClient(priorityQueue);
            } else logger.error("err_type");
        } catch (
                NullPointerException ignored) {
        } finally {
            commandsManager.getLock().unlock();
        }

    }

    /**
     * @param args аргументы команды
     */
    @Override
    public void setArgs(String args) {
        this.args = args;
    }
}
