package lab8.Commands;

import lab8.Collections.SpaceMarine;
import lab8.ManagersAndHandlers.CommandsManager;
import lab8.ManagersAndHandlers.DataBaseManager;

import java.util.PriorityQueue;

/**
 * Класс команды max_by_health
 *
 * @author Остряков Егор, P3112
 */
public class MaxByHealth extends AbstractCommand {
    public MaxByHealth() {
        name = "max by health";
        help = "выводит любой объект из коллекции, значение поля health которого является максимальным";
    }

    /**
     * Выводит элемент коллекции с максимальным здоровьем
     *
     * @param priorityQueue   коллекция, с которой работает пользователь
     * @param commandsManager объект класса CommandsManager
     * @param dataBaseManager объект класса DataBaseManager
     */
    @Override
    public void execute(PriorityQueue<SpaceMarine> priorityQueue, CommandsManager commandsManager, DataBaseManager dataBaseManager) {
        commandsManager.getLock().lock();
        try {
            if (priorityQueue.size() > 0) {
                commandsManager.printToClient("suc_maxByHealth" + "_" + priorityQueue.parallelStream().max(CommandsManager.GetIdComparator()).get());
            } else commandsManager.printToClient("Список пуст");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            commandsManager.getLock().unlock();
        }
    }
}
